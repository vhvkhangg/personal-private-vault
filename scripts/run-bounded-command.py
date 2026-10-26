#!/usr/bin/env python3
"""Start one owned command with bounded execution, progress awareness and owned-tree cleanup."""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
import math
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import threading
import time
from typing import BinaryIO, Callable
import uuid

NORMAL_CAP = 300.0
FULL_CAP = 2100.0
POLL_INTERVAL = 0.5
SAMPLE_INTERVAL = 5.0
INACTIVITY_THRESHOLD = 300.0
DIAGNOSTIC_INTERVAL = 30.0
DIAGNOSTIC_BUDGET = 5.0
CLEANUP_BUDGET = 15.0
TOTAL_GRACE_BUDGET = 20.0
CHUNK_SIZE = 65536

# Hook for tests to inject deterministic hang/deadlock proof or simulated probe results
DIAGNOSTIC_PROBE_OVERRIDE: Callable[[int], tuple[bool, str]] | None = None


def is_full_suite(args: list[str]) -> bool:
    if not args:
        return False
    exe = Path(args[0]).name.lower()
    rest = [x.lower() for x in args[1:]]
    if exe in {'mvn', 'mvn.cmd', 'mvnw', 'mvnw.cmd', 'mvnw.bat'}:
        return ('clean' in rest and 'verify' in rest and not any(x.startswith(('-dtest=', '-dskiptests', '-dmaven.test.skip')) for x in rest))
    if exe in {'python', 'python.exe', 'python3', 'python3.exe', 'py', 'py.exe'}:
        return rest[:2] == ['-m', 'pytest'] and not any(x == '-k' or x.startswith('--ignore') for x in rest)
    return exe in {'pytest', 'pytest.exe'} and not any(x == '-k' or x.startswith('--ignore') for x in rest)


def iso_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec='milliseconds')


def timed_call(fn: Callable, *args, timeout: float = 1.0, default=None, **kwargs):
    """Execute fn(*args, **kwargs) in a daemon thread bounded by timeout. Returns default on timeout or error."""
    if timeout <= 0:
        return default
    result = [default]
    done = threading.Event()

    def worker():
        try:
            result[0] = fn(*args, **kwargs)
        except Exception:
            pass
        finally:
            done.set()

    t = threading.Thread(target=worker, daemon=True)
    t.start()
    done.wait(timeout=max(0.001, timeout))
    return result[0]


def process_running(pid: int) -> bool:
    """Determine whether an owned PID is still executing (exclude POSIX zombies)."""
    if os.name == 'nt':
        import ctypes
        from ctypes import wintypes
        kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)
        kernel32.OpenProcess.argtypes = [wintypes.DWORD, wintypes.BOOL, wintypes.DWORD]
        kernel32.OpenProcess.restype = wintypes.HANDLE
        kernel32.WaitForSingleObject.argtypes = [wintypes.HANDLE, wintypes.DWORD]
        kernel32.WaitForSingleObject.restype = wintypes.DWORD
        kernel32.CloseHandle.argtypes = [wintypes.HANDLE]
        kernel32.CloseHandle.restype = wintypes.BOOL
        handle = kernel32.OpenProcess(0x00100000, False, pid)
        if not handle:
            return False
        try:
            return kernel32.WaitForSingleObject(handle, 0) == 0x102
        finally:
            kernel32.CloseHandle(handle)
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except OSError:
        return False
    proc_stat = Path(f'/proc/{pid}/stat')
    if proc_stat.exists():
        state = proc_stat.read_text().rsplit(')', 1)[1].strip().split()[0]
        return state != 'Z'
    return True


def find_owned_descendants(parent_pid: int) -> list[int]:
    """Find all alive descendant process PIDs of an owned parent process."""
    if os.name == 'nt':
        import ctypes
        from ctypes import wintypes
        kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)
        TH32CS_SNAPPROCESS = 0x00000002
        ERROR_NO_MORE_FILES = 18

        class PROCESSENTRY32(ctypes.Structure):
            _fields_ = [
                ('dwSize', wintypes.DWORD),
                ('cntUsage', wintypes.DWORD),
                ('th32ProcessID', wintypes.DWORD),
                ('th32DefaultHeapID', ctypes.POINTER(wintypes.ULONG)),
                ('th32ModuleID', wintypes.DWORD),
                ('cntThreads', wintypes.DWORD),
                ('th32ParentProcessID', wintypes.DWORD),
                ('pcPriClassBase', ctypes.c_long),
                ('dwFlags', wintypes.DWORD),
                ('szExeFile', ctypes.c_char * 260),
            ]

        kernel32.CreateToolhelp32Snapshot.argtypes = [wintypes.DWORD, wintypes.DWORD]
        kernel32.CreateToolhelp32Snapshot.restype = wintypes.HANDLE
        kernel32.Process32First.argtypes = [wintypes.HANDLE, ctypes.POINTER(PROCESSENTRY32)]
        kernel32.Process32First.restype = wintypes.BOOL
        kernel32.Process32Next.argtypes = [wintypes.HANDLE, ctypes.POINTER(PROCESSENTRY32)]
        kernel32.Process32Next.restype = wintypes.BOOL
        kernel32.CloseHandle.argtypes = [wintypes.HANDLE]
        kernel32.CloseHandle.restype = wintypes.BOOL

        snapshot = kernel32.CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        if not snapshot or snapshot == -1:
            err = ctypes.get_last_error()
            raise OSError(err, f'CreateToolhelp32Snapshot failed with code {err}')

        parent_map: dict[int, list[int]] = {}
        entry = PROCESSENTRY32()
        entry.dwSize = ctypes.sizeof(PROCESSENTRY32)
        try:
            if kernel32.Process32First(snapshot, ctypes.byref(entry)):
                while True:
                    ppid = entry.th32ParentProcessID
                    cpid = entry.th32ProcessID
                    parent_map.setdefault(ppid, []).append(cpid)
                    if not kernel32.Process32Next(snapshot, ctypes.byref(entry)):
                        err = ctypes.get_last_error()
                        if err != ERROR_NO_MORE_FILES:
                            raise OSError(err, f'Process32Next failed with code {err}')
                        break
            else:
                err = ctypes.get_last_error()
                if err != ERROR_NO_MORE_FILES:
                    raise OSError(err, f'Process32First failed with code {err}')
        finally:
            kernel32.CloseHandle(snapshot)

        descendants: list[int] = []
        queue = list(parent_map.get(parent_pid, []))
        while queue:
            curr = queue.pop(0)
            if curr not in descendants:
                if process_running(curr):
                    descendants.append(curr)
                queue.extend(parent_map.get(curr, []))
        return descendants
    else:
        descendants = []
        try:
            for p in Path('/proc').iterdir():
                if p.is_dir() and p.name.isdigit():
                    status = p / 'status'
                    if status.exists():
                        for line in status.read_text().splitlines():
                            if line.startswith('PPid:'):
                                ppid = int(line.split()[1])
                                if ppid == parent_pid:
                                    cpid = int(p.name)
                                    if process_running(cpid):
                                        descendants.append(cpid)
                                        descendants.extend(find_owned_descendants(cpid))
        except Exception as exc:
            raise OSError(f'Failed to inspect /proc: {exc}') from exc
        return descendants


def kill_descendants(pids: list[int], budget: float = CLEANUP_BUDGET) -> bool:
    """Terminate specific owned descendant PIDs within budget."""
    all_killed = True
    start = time.monotonic()
    for pid in pids:
        rem = budget - (time.monotonic() - start)
        if rem <= 0:
            all_killed = False
            break
        if not process_running(pid):
            continue
        if os.name == 'nt':
            command = shutil.which('taskkill.exe') or shutil.which('taskkill')
            if command:
                try:
                    subprocess.run(
                        [command, '/PID', str(pid), '/T', '/F'],
                        stdin=subprocess.DEVNULL,
                        stdout=subprocess.DEVNULL,
                        stderr=subprocess.DEVNULL,
                        timeout=min(5.0, max(0.1, rem)),
                        check=False
                    )
                except Exception:
                    pass
            if process_running(pid):
                try:
                    import ctypes
                    k = ctypes.WinDLL('kernel32', use_last_error=True)
                    h = k.OpenProcess(0x0001, False, pid)
                    if h:
                        k.TerminateProcess(h, 1)
                        k.CloseHandle(h)
                except Exception:
                    pass
        else:
            try:
                os.kill(pid, signal.SIGKILL)
            except Exception:
                pass
        if process_running(pid):
            all_killed = False
    return all_killed


class StreamCapture(threading.Thread):
    """Streams a pipe chunkwise to a file without accumulating the entire stream in memory."""

    def __init__(self, stream, file: BinaryIO | Path | str, file_path: Path | None = None):
        super().__init__(daemon=True)
        self.stream = stream
        if isinstance(file, (str, Path)):
            self.file_path = Path(file)
            self.file = open(self.file_path, 'wb')
            self._owns_file = True
        else:
            self.file = file
            self.file_path = file_path or Path(getattr(file, 'name', 'log'))
            self._owns_file = False
        self.bytes_written = 0
        self.last_write_time = time.monotonic()
        self.error: Exception | None = None
        self.complete = False
        self.tail_buffer = bytearray()

    def run(self) -> None:
        try:
            fd = self.stream.fileno()
            while True:
                chunk = os.read(fd, CHUNK_SIZE)
                if not chunk:
                    break
                self.file.write(chunk)
                self.file.flush()
                self.bytes_written += len(chunk)
                self.last_write_time = time.monotonic()
                self.tail_buffer.extend(chunk)
                if len(self.tail_buffer) > 4096:
                    del self.tail_buffer[:-4096]
            self.complete = True
        except Exception as exc:
            self.error = exc
            self.complete = False
        finally:
            try:
                self.stream.close()
            except Exception:
                pass
            try:
                self.file.close()
            except Exception:
                pass


def get_process_cpu_io(pid: int) -> tuple[int, int] | None:
    """Query attributable CPU ticks and IO operations for a process."""
    if os.name == 'nt':
        import ctypes
        from ctypes import wintypes
        kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)

        class IO_COUNTERS(ctypes.Structure):
            _fields_ = [
                ('ReadOperationCount', ctypes.c_uint64),
                ('WriteOperationCount', ctypes.c_uint64),
                ('OtherOperationCount', ctypes.c_uint64),
                ('ReadTransferCount', ctypes.c_uint64),
                ('WriteTransferCount', ctypes.c_uint64),
                ('OtherTransferCount', ctypes.c_uint64),
            ]

        handle = kernel32.OpenProcess(0x1000, False, pid)
        if not handle:
            handle = kernel32.OpenProcess(0x0400, False, pid)
        if not handle:
            return None
        try:
            ft_create = wintypes.FILETIME()
            ft_exit = wintypes.FILETIME()
            ft_kernel = wintypes.FILETIME()
            ft_user = wintypes.FILETIME()
            kernel32.GetProcessTimes.argtypes = [
                wintypes.HANDLE,
                ctypes.POINTER(wintypes.FILETIME),
                ctypes.POINTER(wintypes.FILETIME),
                ctypes.POINTER(wintypes.FILETIME),
                ctypes.POINTER(wintypes.FILETIME),
            ]
            kernel32.GetProcessTimes.restype = wintypes.BOOL
            ok_times = kernel32.GetProcessTimes(
                handle,
                ctypes.byref(ft_create),
                ctypes.byref(ft_exit),
                ctypes.byref(ft_kernel),
                ctypes.byref(ft_user)
            )
            cpu_ticks = 0
            if ok_times:
                k = (ft_kernel.dwHighDateTime << 32) + ft_kernel.dwLowDateTime
                u = (ft_user.dwHighDateTime << 32) + ft_user.dwLowDateTime
                cpu_ticks = k + u

            io_ops = 0
            kernel32.GetProcessIoCounters.argtypes = [wintypes.HANDLE, ctypes.POINTER(IO_COUNTERS)]
            kernel32.GetProcessIoCounters.restype = wintypes.BOOL
            io_counters = IO_COUNTERS()
            if kernel32.GetProcessIoCounters(handle, ctypes.byref(io_counters)):
                io_ops = io_counters.ReadOperationCount + io_counters.WriteOperationCount + io_counters.OtherOperationCount
            return (cpu_ticks, io_ops)
        finally:
            kernel32.CloseHandle(handle)
    else:
        stat_path = Path(f'/proc/{pid}/stat')
        if stat_path.exists():
            try:
                parts = stat_path.read_text().rsplit(')', 1)[1].split()
                utime = int(parts[11])
                stime = int(parts[12])
                return (utime + stime, 0)
            except Exception:
                pass
    return None


def detect_process_cycle_or_hang(pid: int) -> tuple[bool, str]:
    """Inspect owned process for deadlock/wait cycle using native APIs or test override."""
    if DIAGNOSTIC_PROBE_OVERRIDE is not None:
        return DIAGNOSTIC_PROBE_OVERRIDE(pid)

    if os.name == 'nt':
        import ctypes
        from ctypes import wintypes

        advapi32 = ctypes.WinDLL('advapi32', use_last_error=True)
        kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)

        TH32CS_SNAPTHREAD = 0x00000004
        WCT_MAX_NODE_COUNT = 16
        WCT_OBJNAME_LENGTH = 128

        class THREADENTRY32(ctypes.Structure):
            _fields_ = [
                ('dwSize', wintypes.DWORD),
                ('cntUsage', wintypes.DWORD),
                ('th32ThreadID', wintypes.DWORD),
                ('th32OwnerProcessID', wintypes.DWORD),
                ('tpBasePri', ctypes.c_long),
                ('tpDeltaPri', ctypes.c_long),
                ('dwFlags', wintypes.DWORD),
            ]

        class _LOCK_OBJECT(ctypes.Structure):
            _fields_ = [
                ('ObjectName', wintypes.WCHAR * WCT_OBJNAME_LENGTH),
                ('Timeout', wintypes.LARGE_INTEGER),
                ('Alertable', wintypes.BOOL),
            ]

        class _THREAD_OBJECT(ctypes.Structure):
            _fields_ = [
                ('ProcessId', wintypes.DWORD),
                ('ThreadId', wintypes.DWORD),
                ('WaitTime', wintypes.DWORD),
                ('ContextSwitches', wintypes.DWORD),
            ]

        class _WCT_UNION(ctypes.Union):
            _fields_ = [
                ('LockObject', _LOCK_OBJECT),
                ('ThreadObject', _THREAD_OBJECT),
            ]

        class WAITCHAIN_NODE_INFO(ctypes.Structure):
            _fields_ = [
                ('ObjectType', wintypes.DWORD),
                ('ObjectStatus', wintypes.DWORD),
                ('u', _WCT_UNION),
            ]

        advapi32.OpenThreadWaitChainSession.argtypes = [wintypes.DWORD, ctypes.c_void_p]
        advapi32.OpenThreadWaitChainSession.restype = ctypes.c_void_p
        advapi32.GetThreadWaitChain.argtypes = [
            ctypes.c_void_p,
            ctypes.c_void_p,
            wintypes.DWORD,
            wintypes.DWORD,
            ctypes.POINTER(wintypes.DWORD),
            ctypes.POINTER(WAITCHAIN_NODE_INFO * WCT_MAX_NODE_COUNT),
            ctypes.POINTER(wintypes.BOOL)
        ]
        advapi32.GetThreadWaitChain.restype = wintypes.BOOL
        advapi32.CloseThreadWaitChainSession.argtypes = [ctypes.c_void_p]
        advapi32.CloseThreadWaitChainSession.restype = None

        kernel32.CreateToolhelp32Snapshot.argtypes = [wintypes.DWORD, wintypes.DWORD]
        kernel32.CreateToolhelp32Snapshot.restype = wintypes.HANDLE
        kernel32.Thread32First.argtypes = [wintypes.HANDLE, ctypes.POINTER(THREADENTRY32)]
        kernel32.Thread32First.restype = wintypes.BOOL
        kernel32.Thread32Next.argtypes = [wintypes.HANDLE, ctypes.POINTER(THREADENTRY32)]
        kernel32.Thread32Next.restype = wintypes.BOOL
        kernel32.CloseHandle.argtypes = [wintypes.HANDLE]
        kernel32.CloseHandle.restype = wintypes.BOOL

        snapshot = kernel32.CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0)
        if not snapshot or snapshot == -1:
            return (False, 'toolhelp_snapshot_failed')
        threads: list[int] = []
        entry = THREADENTRY32()
        entry.dwSize = ctypes.sizeof(THREADENTRY32)
        try:
            if kernel32.Thread32First(snapshot, ctypes.byref(entry)):
                while True:
                    if entry.th32OwnerProcessID == pid:
                        threads.append(entry.th32ThreadID)
                    if not kernel32.Thread32Next(snapshot, ctypes.byref(entry)):
                        break
        finally:
            kernel32.CloseHandle(snapshot)

        if not threads:
            return (False, 'no_threads_found')

        session = advapi32.OpenThreadWaitChainSession(0, None)
        if not session:
            return (False, 'wct_session_open_failed')
        try:
            for tid in threads:
                node_count = wintypes.DWORD(WCT_MAX_NODE_COUNT)
                nodes = (WAITCHAIN_NODE_INFO * WCT_MAX_NODE_COUNT)()
                is_cycle = wintypes.BOOL(False)
                res = advapi32.GetThreadWaitChain(
                    session,
                    None,
                    0,
                    tid,
                    ctypes.byref(node_count),
                    ctypes.byref(nodes),
                    ctypes.byref(is_cycle)
                )
                if res and is_cycle.value:
                    return (True, f'wait_chain_cycle_detected:thread={tid}')
            return (False, f'checked_threads={len(threads)}')
        finally:
            advapi32.CloseThreadWaitChainSession(session)

    return (False, 'native_wct_unsupported_on_platform')


def run_diagnostic_with_budget(pid: int, budget: float) -> tuple[bool, str, float]:
    """Execute process hang/deadlock probe with a strict timeout budget."""
    t_start = time.monotonic()
    if budget <= 0.005:
        return False, 'diagnostic_skipped_budget_exhausted', 0.0

    result = [False, 'probes_timed_out']

    def worker():
        try:
            result[0], result[1] = detect_process_cycle_or_hang(pid)
        except Exception:
            result[1] = 'probe_exception'

    th = threading.Thread(target=worker, daemon=True)
    th.start()
    th.join(timeout=budget)
    dur = time.monotonic() - t_start
    if th.is_alive():
        return False, f'diagnostic_timed_out_after_{dur:.3f}s', dur
    return result[0], result[1], dur


def check_progress_files(paths: list[Path]) -> tuple[int, float]:
    """Check total size and maximum mtime across explicit progress files."""
    total_size = 0
    max_mtime = 0.0
    for p in paths:
        try:
            st = p.stat()
            total_size += st.st_size
            if st.st_mtime > max_mtime:
                max_mtime = st.st_mtime
        except (OSError, FileNotFoundError):
            pass
    return (total_size, max_mtime)


def write_receipt(path: Path | None, data: dict, file_obj: BinaryIO | None = None) -> None:
    """Write receipt JSON exclusively to ensure no prior file is overwritten or truncated."""
    if path is None and file_obj is None:
        return
    encoded = json.dumps(data, indent=2).encode('utf-8') + b'\n'
    if file_obj is not None:
        try:
            if not file_obj.closed:
                file_obj.seek(0)
                file_obj.truncate(0)
                file_obj.write(encoded)
                file_obj.flush()
                file_obj.close()
                return
        except Exception:
            pass
    if path is not None:
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, 'x', encoding='utf-8') as f:
            f.write(encoded.decode('utf-8'))


def kill_owned_tree(proc: subprocess.Popen, budget: float = CLEANUP_BUDGET) -> str:
    status_parts = []
    start = time.monotonic()
    if proc.poll() is None:
        if os.name == 'nt':
            command = shutil.which('taskkill.exe') or shutil.which('taskkill')
            if command:
                rem = max(0.1, budget - (time.monotonic() - start))
                try:
                    res = subprocess.run(
                        [command, '/PID', str(proc.pid), '/T', '/F'],
                        stdin=subprocess.DEVNULL,
                        stdout=subprocess.DEVNULL,
                        stderr=subprocess.DEVNULL,
                        timeout=min(5.0, rem),
                        check=False
                    )
                    if res.returncode == 0:
                        status_parts.append('windows_tree_termination_requested')
                except (OSError, subprocess.TimeoutExpired):
                    pass
            if proc.poll() is None:
                proc.kill()
                status_parts.append('windows_parent_killed_tree_unverified')
        else:
            try:
                os.killpg(proc.pid, signal.SIGKILL)
                status_parts.append('posix_process_group_killed')
            except ProcessLookupError:
                status_parts.append('already_exited')
            except OSError:
                if proc.poll() is None:
                    proc.kill()
                    status_parts.append('posix_parent_killed_tree_unverified')
    else:
        status_parts.append('already_exited')

    rem = max(0.0, budget - (time.monotonic() - start))
    if rem > 0:
        descendants = timed_call(find_owned_descendants, proc.pid, timeout=min(1.0, rem), default=None)
        if descendants:
            rem = max(0.0, budget - (time.monotonic() - start))
            if rem > 0:
                timed_call(kill_descendants, descendants, budget=rem, timeout=rem, default=False)
                status_parts.append('descendants_terminated')
            else:
                status_parts.append('descendant_cleanup_timeout')

    return ';'.join(status_parts) if status_parts else 'not_needed'


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', choices=['normal', 'full-suite'], default='normal')
    parser.add_argument('--timeout-seconds', type=str)
    parser.add_argument('--receipt', type=Path)
    parser.add_argument('--progress-file', type=Path, action='append', default=[])
    parser.add_argument('command', nargs=argparse.REMAINDER)
    args = parser.parse_args(argv)

    cmd = args.command[1:] if args.command[:1] == ['--'] else args.command
    if not cmd:
        parser.error('Supply executable and arguments after --')

    cap = FULL_CAP if args.profile == 'full-suite' else NORMAL_CAP
    if args.profile == 'full-suite' and not is_full_suite(cmd):
        parser.error('Extended timeout requires an actual full-test-suite command')

    if args.timeout_seconds is not None:
        try:
            timeout = float(args.timeout_seconds)
        except (ValueError, TypeError):
            parser.error(f'invalid timeout-seconds: {args.timeout_seconds}')
        if math.isnan(timeout) or not math.isfinite(timeout) or timeout <= 0 or timeout > cap:
            parser.error(f'timeout must be >0 and <= {int(cap)} seconds (must be finite)')
    else:
        timeout = float(cap)

    # Resolve paths exclusively before launch
    if args.receipt is not None:
        receipt_path = args.receipt.resolve()
        stdout_path = receipt_path.with_name(receipt_path.stem + '.stdout.log')
        stderr_path = receipt_path.with_name(receipt_path.stem + '.stderr.log')
        if receipt_path.exists():
            parser.error(f'Receipt file already exists: {receipt_path}')
        if stdout_path.exists():
            parser.error(f'Log file already exists: {stdout_path}')
        if stderr_path.exists():
            parser.error(f'Log file already exists: {stderr_path}')
    else:
        run_id = f'run-{int(time.time())}-{uuid.uuid4().hex[:8]}'
        log_dir = Path('logs/bounded-runs').resolve()
        log_dir.mkdir(parents=True, exist_ok=True)
        receipt_path = log_dir / f'{run_id}.json'
        stdout_path = log_dir / f'{run_id}.stdout.log'
        stderr_path = log_dir / f'{run_id}.stderr.log'

    receipt_path.parent.mkdir(parents=True, exist_ok=True)
    stdout_file = None
    stderr_file = None
    receipt_file = None
    try:
        stdout_file = open(stdout_path, 'xb')
        stderr_file = open(stderr_path, 'xb')
        receipt_file = open(receipt_path, 'xb')
    except FileExistsError:
        for f, p in [(stdout_file, stdout_path), (stderr_file, stderr_path), (receipt_file, receipt_path)]:
            if f is not None:
                try:
                    f.close()
                except Exception:
                    pass
                try:
                    p.unlink(missing_ok=True)
                except Exception:
                    pass
        print(f'[bounded] collision: FileExistsError', file=sys.stderr, flush=True)
        return 1

    start_monotonic = time.monotonic()
    deadline = start_monotonic + timeout
    started_utc = iso_now()

    executable_name = Path(cmd[0]).name
    argv_sha256 = hashlib.sha256('\0'.join(cmd).encode()).hexdigest()

    data: dict = {
        'profile': args.profile,
        'timeout_seconds': timeout,
        'executable': executable_name,
        'argv_sha256': argv_sha256,
        'started_utc': started_utc,
        'stdout_path': str(stdout_path),
        'stderr_path': str(stderr_path),
        'diagnostic_observations': [],
    }

    print(f'[bounded] starting {executable_name} (limit {timeout:g}s)', flush=True)

    try:
        proc = subprocess.Popen(
            cmd,
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            start_new_session=os.name != 'nt',
            creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == 'nt' else 0
        )
    except OSError:
        if stdout_file:
            stdout_file.close()
        if stderr_file:
            stderr_file.close()
        if receipt_file and not receipt_file.closed:
            receipt_file.close()
        try:
            receipt_path.unlink(missing_ok=True)
        except Exception:
            pass
        print(f'[bounded] launch failed', file=sys.stderr, flush=True)
        data.update(
            result='LAUNCH_ERROR',
            exit_code=127,
            finished_utc=iso_now(),
            duration_seconds=round(time.monotonic() - start_monotonic, 3),
            execution_duration_seconds=0.0,
            diagnostic_duration_seconds=0.0,
            cleanup_duration_seconds=0.0,
            cleanup='not_needed',
            capture_complete=False,
            cleanup_complete=True,
            stdout_bytes=0,
            stderr_bytes=0,
        )
        write_receipt(receipt_path, data)
        return 127

    stdout_capture = None
    stderr_capture = None
    terminal_result = 'UNKNOWN'
    exit_code = 1
    cleanup = 'not_needed'
    diagnostic_total_duration = 0.0
    execution_end_time = None
    shutdown_deadline = None

    try:
        stdout_capture = StreamCapture(proc.stdout, stdout_file, stdout_path)
        stderr_capture = StreamCapture(proc.stderr, stderr_file, stderr_path)
        stdout_capture.start()
        stderr_capture.start()

        last_progress_time = start_monotonic
        last_sample_time = start_monotonic
        last_diagnostic_time = 0.0
        prev_bytes = 0

        rem_init = max(0.001, deadline - time.monotonic())
        prev_prog_files = timed_call(check_progress_files, args.progress_file, timeout=min(DIAGNOSTIC_BUDGET, rem_init), default=(0, 0.0))
        rem_init = max(0.001, deadline - time.monotonic())
        prev_cpu_io = timed_call(get_process_cpu_io, proc.pid, timeout=min(DIAGNOSTIC_BUDGET, rem_init), default=None)

        while True:
            now = time.monotonic()

            code = proc.poll()
            if code is not None:
                execution_end_time = time.monotonic()
                shutdown_deadline = execution_end_time + TOTAL_GRACE_BUDGET
                exit_code = code
                terminal_result = 'PASS' if exit_code == 0 else 'COMMAND_FAILED'
                break

            if now >= deadline:
                execution_end_time = deadline
                shutdown_deadline = deadline + TOTAL_GRACE_BUDGET
                rem_diag = max(0.001, shutdown_deadline - time.monotonic())
                diag_budget = min(DIAGNOSTIC_BUDGET, 2.0, rem_diag)
                is_hung, details, diag_dur = run_diagnostic_with_budget(proc.pid, diag_budget)
                diagnostic_total_duration += diag_dur
                data['diagnostic_observations'].append({
                    'timestamp_utc': iso_now(),
                    'elapsed_seconds': round(now - start_monotonic, 3),
                    'inactivity_seconds': round(now - last_progress_time, 3),
                    'diagnosis': 'HUNG_CONFIRMED' if is_hung else 'TIMEOUT_SNAPSHOT',
                    'details': details,
                    'duration_seconds': round(diag_dur, 3)
                })
                if is_hung:
                    terminal_result = 'HUNG_CONFIRMED'
                    exit_code = 125
                else:
                    terminal_result = 'TIMEOUT'
                    exit_code = 124
                break

            if (now - last_sample_time) >= SAMPLE_INTERVAL:
                last_sample_time = now
                current_bytes = stdout_capture.bytes_written + stderr_capture.bytes_written
                if current_bytes > prev_bytes:
                    last_progress_time = now
                    prev_bytes = current_bytes

                rem_sample = max(0.001, deadline - now)
                curr_prog_files = timed_call(check_progress_files, args.progress_file, timeout=min(DIAGNOSTIC_BUDGET, rem_sample), default=prev_prog_files)
                if curr_prog_files != prev_prog_files:
                    last_progress_time = now
                    prev_prog_files = curr_prog_files

                rem_sample = max(0.001, deadline - time.monotonic())
                curr_cpu_io = timed_call(get_process_cpu_io, proc.pid, timeout=min(DIAGNOSTIC_BUDGET, rem_sample), default=prev_cpu_io)
                if curr_cpu_io is not None and prev_cpu_io is not None:
                    if curr_cpu_io[0] > prev_cpu_io[0] or curr_cpu_io[1] > prev_cpu_io[1]:
                        last_progress_time = now
                if curr_cpu_io is not None:
                    prev_cpu_io = curr_cpu_io

            inactivity = now - last_progress_time
            if inactivity >= INACTIVITY_THRESHOLD:
                if (now - last_diagnostic_time) >= DIAGNOSTIC_INTERVAL:
                    last_diagnostic_time = now
                    diag_budget = min(DIAGNOSTIC_BUDGET, max(0.001, deadline - now))
                    is_hung, details, diag_dur = run_diagnostic_with_budget(proc.pid, diag_budget)
                    diagnostic_total_duration += diag_dur

                    obs = {
                        'timestamp_utc': iso_now(),
                        'elapsed_seconds': round(now - start_monotonic, 3),
                        'inactivity_seconds': round(inactivity, 3),
                        'diagnosis': 'HUNG_CONFIRMED' if is_hung else 'DIAGNOSTIC_INCONCLUSIVE',
                        'details': details,
                        'duration_seconds': round(diag_dur, 3)
                    }
                    data['diagnostic_observations'].append(obs)

                    if is_hung:
                        execution_end_time = now
                        shutdown_deadline = now + TOTAL_GRACE_BUDGET
                        print(f'[bounded] diagnostic at {obs["elapsed_seconds"]}s: HUNG_CONFIRMED', flush=True)
                        terminal_result = 'HUNG_CONFIRMED'
                        exit_code = 125
                        break
                    else:
                        print(f'[bounded] diagnostic at {obs["elapsed_seconds"]}s: inconclusive', flush=True)

            sleep_time = min(POLL_INTERVAL, max(0.01, deadline - now))
            time.sleep(sleep_time)

    except KeyboardInterrupt:
        if execution_end_time is None:
            execution_end_time = time.monotonic()
        if shutdown_deadline is None:
            shutdown_deadline = execution_end_time + TOTAL_GRACE_BUDGET
        terminal_result = 'CANCELLED'
        exit_code = 130
    except Exception as exc:
        if execution_end_time is None:
            execution_end_time = time.monotonic()
        if shutdown_deadline is None:
            shutdown_deadline = execution_end_time + TOTAL_GRACE_BUDGET
        terminal_result = 'INTERNAL_ERROR'
        exit_code = 1
        data['internal_error'] = f'unexpected_monitor_exception_{type(exc).__name__}'
        raise
    finally:
        if execution_end_time is None:
            execution_end_time = time.monotonic()
        if shutdown_deadline is None:
            shutdown_deadline = execution_end_time + TOTAL_GRACE_BUDGET
        cleanup_start = time.monotonic()
        exec_duration = execution_end_time - start_monotonic

        if terminal_result in {'TIMEOUT', 'HUNG_CONFIRMED', 'CANCELLED', 'INTERNAL_ERROR'} or proc.poll() is None:
            rem = max(0.01, shutdown_deadline - time.monotonic())
            cleanup = kill_owned_tree(proc, budget=min(CLEANUP_BUDGET, rem))
            rem = max(0.01, shutdown_deadline - time.monotonic())
            try:
                proc.wait(timeout=min(1.0, rem))
            except subprocess.TimeoutExpired:
                cleanup += ';unreaped_process'
        else:
            cleanup = 'not_needed'

        rem_drain = max(0.001, shutdown_deadline - time.monotonic())
        if stdout_capture and rem_drain > 0:
            try:
                stdout_capture.join(timeout=min(1.0, rem_drain))
            except RuntimeError:
                pass
        rem_drain = max(0.001, shutdown_deadline - time.monotonic())
        if stderr_capture and rem_drain > 0:
            try:
                stderr_capture.join(timeout=min(1.0, rem_drain))
            except RuntimeError:
                pass

        try:
            if stdout_file and not stdout_file.closed:
                stdout_file.close()
        except Exception:
            pass
        try:
            if stderr_file and not stderr_file.closed:
                stderr_file.close()
        except Exception:
            pass
        try:
            if stdout_capture and not stdout_capture.is_alive() and proc.stdout and not proc.stdout.closed:
                proc.stdout.close()
        except Exception:
            pass
        try:
            if stderr_capture and not stderr_capture.is_alive() and proc.stderr and not proc.stderr.closed:
                proc.stderr.close()
        except Exception:
            pass

        capture_complete = (
            (stdout_capture is not None and stdout_capture.complete and stdout_capture.error is None)
            and (stderr_capture is not None and stderr_capture.complete and stderr_capture.error is None)
        )

        descendants_verified = False
        surviving_descendants = None
        rem_grace = max(0.0, shutdown_deadline - time.monotonic())
        if rem_grace > 0:
            res = timed_call(find_owned_descendants, proc.pid, timeout=min(1.0, rem_grace), default=None)
            if isinstance(res, list):
                descendants_verified = True
                surviving_descendants = res

        if surviving_descendants:
            rem_grace = max(0.0, shutdown_deadline - time.monotonic())
            kill_ok = False
            if rem_grace > 0:
                kill_ok = timed_call(kill_descendants, surviving_descendants, budget=rem_grace, timeout=min(CLEANUP_BUDGET, rem_grace), default=False)
                cleanup = 'windows_owned_descendants_terminated' if cleanup == 'not_needed' else cleanup + ';descendants_terminated'
            if not kill_ok:
                cleanup += ';descendant_cleanup_timeout'
                descendants_verified = False
            else:
                rem_grace = max(0.0, shutdown_deadline - time.monotonic())
                if rem_grace > 0:
                    res2 = timed_call(find_owned_descendants, proc.pid, timeout=min(1.0, rem_grace), default=None)
                    if isinstance(res2, list):
                        descendants_verified = True
                        surviving_descendants = res2
                    else:
                        descendants_verified = False
                else:
                    descendants_verified = False

        if not descendants_verified:
            cleanup_complete = False
            cleanup = cleanup + ';descendant_verification_unavailable' if cleanup != 'not_needed' else 'descendant_verification_unavailable'
        else:
            cleanup_complete = (proc.poll() is not None) and (len(surviving_descendants) == 0)

        if terminal_result == 'PASS':
            if exit_code != 0 or not capture_complete or not cleanup_complete:
                terminal_result = 'COMMAND_FAILED'
                exit_code = exit_code if exit_code != 0 else 1

        cleanup_duration = time.monotonic() - cleanup_start
        total_duration = time.monotonic() - start_monotonic
        finished_utc = iso_now()

        data.update({
            'pid': proc.pid,
            'result': terminal_result,
            'exit_code': exit_code,
            'cleanup': cleanup,
            'duration_seconds': round(total_duration, 3),
            'execution_duration_seconds': round(exec_duration, 3),
            'diagnostic_duration_seconds': round(diagnostic_total_duration, 3),
            'cleanup_duration_seconds': round(cleanup_duration, 3),
            'finished_utc': finished_utc,
            'stdout_bytes': stdout_capture.bytes_written if stdout_capture else 0,
            'stderr_bytes': stderr_capture.bytes_written if stderr_capture else 0,
            'capture_complete': capture_complete,
            'cleanup_complete': cleanup_complete,
        })

        receipt_written = False
        try:
            write_receipt(receipt_path, data, file_obj=receipt_file)
            receipt_written = True
        except Exception as exc:
            print(f'[bounded] evidence write failed: {type(exc).__name__}', file=sys.stderr, flush=True)
            if receipt_file and not receipt_file.closed:
                try:
                    receipt_file.close()
                except Exception:
                    pass
            try:
                receipt_path.unlink(missing_ok=True)
            except Exception:
                pass
            if terminal_result == 'PASS':
                terminal_result = 'COMMAND_FAILED'
                exit_code = 1
            data['receipt_write_error'] = 'evidence_write_failed'
        finally:
            try:
                if receipt_file and not receipt_file.closed:
                    receipt_file.close()
            except Exception:
                pass

        print(f'[bounded] {terminal_result} exit={exit_code} cleanup={cleanup}', flush=True)
        if receipt_written:
            print(f'[bounded] receipt: {receipt_path}', flush=True)

    if terminal_result == 'TIMEOUT':
        return 124
    if terminal_result == 'HUNG_CONFIRMED':
        return 125
    if terminal_result == 'CANCELLED':
        return 130
    return exit_code if 0 <= exit_code <= 125 else 1


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print(f'[bounded] INTERNAL_ERROR {type(exc).__name__}', file=sys.stderr, flush=True)
        sys.exit(1)
