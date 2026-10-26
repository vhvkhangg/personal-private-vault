"""Focused tests and native Windows integration tests for bounded-command runner (Maintenance B1).

Covers requirements B1-01 through B1-12.
"""
from __future__ import annotations

import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
import unittest
from unittest.mock import patch

WRAPPER = Path(__file__).with_name('run-bounded-command.py')
spec = importlib.util.spec_from_file_location('bounded', WRAPPER)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


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
        # SYNCHRONIZE (0x00100000) is enough for this process-lifetime probe.
        handle = kernel32.OpenProcess(0x00100000, False, pid)
        if not handle:
            error = ctypes.get_last_error()
            if error == 87:  # ERROR_INVALID_PARAMETER: PID no longer exists.
                return False
            raise OSError(error, f'Cannot inspect owned PID {pid}')
        try:
            code = kernel32.WaitForSingleObject(handle, 0)
            if code == 0:  # WAIT_OBJECT_0: terminated.
                return False
            if code == 0x102:  # WAIT_TIMEOUT: still running.
                return True
            raise OSError(f'Unexpected WaitForSingleObject result for {pid}: {code}')
        finally:
            kernel32.CloseHandle(handle)
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except PermissionError as exc:
        raise AssertionError(f'Cannot inspect owned PID {pid}') from exc
    proc_stat = Path(f'/proc/{pid}/stat')
    if proc_stat.exists():
        state = proc_stat.read_text().rsplit(')', 1)[1].strip().split()[0]
        return state != 'Z'
    return True


class BoundedCommandTests(unittest.TestCase):
    def setUp(self):
        # Reset any module overrides between tests
        module.DIAGNOSTIC_PROBE_OVERRIDE = None
        module.NORMAL_CAP = 300.0
        module.FULL_CAP = 2100.0
        module.POLL_INTERVAL = 0.5
        module.SAMPLE_INTERVAL = 5.0
        module.INACTIVITY_THRESHOLD = 300.0
        module.DIAGNOSTIC_INTERVAL = 30.0

    def tearDown(self):
        module.DIAGNOSTIC_PROBE_OVERRIDE = None

    def test_b1_01_ordinary_child_exit_zero_defaults(self):
        """B1-01: Ordinary child exits 0 in seconds; default deadline 300s; PASS receipt."""
        self.assertEqual(300.0, module.NORMAL_CAP)
        self.assertEqual(2100.0, module.FULL_CAP)
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'ok.json'
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'print("hello stdout"); import sys; sys.stderr.write("hello stderr\\n")'
            ], capture_output=True, text=True, timeout=10)
            self.assertEqual(0, p.returncode, p.stderr)
            self.assertIn('[bounded] starting', p.stdout)
            self.assertIn('[bounded] PASS exit=0', p.stdout)

            receipt = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('PASS', receipt['result'])
            self.assertEqual(0, receipt['exit_code'])
            self.assertEqual(300.0, receipt['timeout_seconds'])
            self.assertNotIn('argv', receipt)
            self.assertTrue(receipt['capture_complete'])
            self.assertTrue(receipt['cleanup_complete'])
            self.assertGreater(receipt['stdout_bytes'], 0)
            self.assertGreater(receipt['stderr_bytes'], 0)

            stdout_file = Path(receipt['stdout_path'])
            stderr_file = Path(receipt['stderr_path'])
            self.assertTrue(stdout_file.exists())
            self.assertTrue(stderr_file.exists())
            self.assertIn('hello stdout', stdout_file.read_text(encoding='utf-8'))
            self.assertIn('hello stderr', stderr_file.read_text(encoding='utf-8'))

    def test_b1_02_deadline_exceeded_timeout_124(self):
        """B1-02: Ordinary child alive at deadline; TIMEOUT, exit 124, durations recorded."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'timeout.json'
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--timeout-seconds', '0.3',
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'import time; time.sleep(5)'
            ], capture_output=True, text=True, timeout=10)
            self.assertEqual(124, p.returncode, p.stderr)
            receipt = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('TIMEOUT', receipt['result'])
            self.assertEqual(124, receipt['exit_code'])
            self.assertGreaterEqual(receipt['execution_duration_seconds'], 0.25)
            self.assertGreaterEqual(receipt['cleanup_duration_seconds'], 0.0)

    def test_b1_03_profile_guard_and_extended_cap(self):
        """B1-03: Full-suite profile eligibility and cap check."""
        self.assertTrue(module.is_full_suite(['mvn', '-ntp', 'clean', 'verify']))
        self.assertTrue(module.is_full_suite(['python', '-m', 'pytest', 'tests']))
        self.assertTrue(module.is_full_suite(['pytest', 'tests']))
        self.assertFalse(module.is_full_suite(['mvn', 'test']))
        self.assertFalse(module.is_full_suite(['mvn', '-Dtest=Foo', 'clean', 'verify']))
        self.assertFalse(module.is_full_suite(['pip', 'install', 'torch']))

        # Reject non-suite command with full-suite profile
        p1 = subprocess.run([
            sys.executable, str(WRAPPER),
            '--profile', 'full-suite',
            '--', sys.executable, '-c', 'pass'
        ], capture_output=True, text=True, timeout=10)
        self.assertNotEqual(0, p1.returncode)
        self.assertIn('full-test-suite', p1.stderr)

        # Reject timeout exceeding FULL_CAP
        p2 = subprocess.run([
            sys.executable, str(WRAPPER),
            '--profile', 'full-suite',
            '--timeout-seconds', '2500',
            '--', 'pytest'
        ], capture_output=True, text=True, timeout=10)
        self.assertNotEqual(0, p2.returncode)
        self.assertIn('timeout must be >0 and <= 2100', p2.stderr)

        # Reject timeout <= 0
        p3 = subprocess.run([
            sys.executable, str(WRAPPER),
            '--timeout-seconds', '0',
            '--', sys.executable, '-c', 'pass'
        ], capture_output=True, text=True, timeout=10)
        self.assertNotEqual(0, p3.returncode)
        self.assertIn('timeout must be >0', p3.stderr)

    def test_b1_04_attributable_progress_resets_inactivity(self):
        """B1-04: Silent child making attributable file or CPU progress does not trigger hang."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'prog.json'
            prog_file = Path(tmp) / 'report.txt'
            prog_file.write_text('start\n', encoding='utf-8')

            module.INACTIVITY_THRESHOLD = 0.4
            module.POLL_INTERVAL = 0.05
            module.SAMPLE_INTERVAL = 0.1
            module.DIAGNOSTIC_INTERVAL = 0.2

            child_code = (
                "import time, sys\n"
                "from pathlib import Path\n"
                "p = Path(sys.argv[1])\n"
                "for i in range(4):\n"
                "    time.sleep(0.15)\n"
                "    p.write_text(f'step {i}\\n', encoding='utf-8')\n"
            )

            code = module.main([
                '--timeout-seconds', '5',
                '--receipt', str(receipt_path),
                '--progress-file', str(prog_file),
                '--', sys.executable, '-c', child_code, str(prog_file)
            ])
            self.assertEqual(0, code)
            receipt = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('PASS', receipt['result'])
            self.assertEqual(0, len(receipt['diagnostic_observations']))

            # Also verify CPU-intensive work resets inactivity
            receipt_cpu = Path(tmp) / 'cpu.json'
            cpu_code = (
                "import time\n"
                "start = time.monotonic()\n"
                "s = 0\n"
                "while time.monotonic() - start < 0.6:\n"
                "    s += 1\n"
            )
            code2 = module.main([
                '--timeout-seconds', '5',
                '--receipt', str(receipt_cpu),
                '--', sys.executable, '-c', cpu_code
            ])
            self.assertEqual(0, code2)
            receipt2 = json.loads(receipt_cpu.read_text(encoding='utf-8'))
            self.assertEqual('PASS', receipt2['result'])
            self.assertEqual(0, len(receipt2['diagnostic_observations']))

    def test_b1_05_inactivity_confirmed_hang_early_kill(self):
        """B1-05: Objective hang/deadlock proof yields HUNG_CONFIRMED and early child kill."""
        with tempfile.TemporaryDirectory() as tmp:
            # 1. Structured injection test
            receipt_mock = Path(tmp) / 'hung_mock.json'
            module.INACTIVITY_THRESHOLD = 0.3
            module.POLL_INTERVAL = 0.05
            module.SAMPLE_INTERVAL = 0.05
            module.DIAGNOSTIC_INTERVAL = 0.1
            module.DIAGNOSTIC_PROBE_OVERRIDE = lambda pid: (True, 'mock_wait_cycle_proven')

            code = module.main([
                '--timeout-seconds', '10',
                '--receipt', str(receipt_mock),
                '--', sys.executable, '-c', 'import time; time.sleep(10)'
            ])
            self.assertEqual(125, code)
            data = json.loads(receipt_mock.read_text(encoding='utf-8'))
            self.assertEqual('HUNG_CONFIRMED', data['result'])
            self.assertEqual(125, data['exit_code'])
            self.assertIn('mock_wait_cycle_proven', data['diagnostic_observations'][0]['details'])

            # 2. Native Windows WCT deadlock test
            if os.name == 'nt':
                module.DIAGNOSTIC_PROBE_OVERRIDE = None  # Native WCT probe
                receipt_native = Path(tmp) / 'hung_native.json'
                deadlock_script = Path(tmp) / 'deadlock.py'
                deadlock_script.write_text('\n'.join([
                    'import ctypes, threading, time',
                    'from ctypes import wintypes',
                    'k = ctypes.WinDLL("kernel32", use_last_error=True)',
                    'k.CreateMutexW.argtypes = [ctypes.c_void_p, wintypes.BOOL, wintypes.LPCWSTR]',
                    'k.CreateMutexW.restype = wintypes.HANDLE',
                    'k.WaitForSingleObject.argtypes = [wintypes.HANDLE, wintypes.DWORD]',
                    'k.WaitForSingleObject.restype = wintypes.DWORD',
                    'm1 = k.CreateMutexW(None, False, None)',
                    'm2 = k.CreateMutexW(None, False, None)',
                    'def t1():',
                    '    k.WaitForSingleObject(m1, 0xFFFFFFFF)',
                    '    time.sleep(0.1)',
                    '    k.WaitForSingleObject(m2, 0xFFFFFFFF)',
                    'def t2():',
                    '    k.WaitForSingleObject(m2, 0xFFFFFFFF)',
                    '    time.sleep(0.1)',
                    '    k.WaitForSingleObject(m1, 0xFFFFFFFF)',
                    'threading.Thread(target=t1, daemon=True).start()',
                    'threading.Thread(target=t2, daemon=True).start()',
                    'while True: time.sleep(1)',
                ]) + '\n', encoding='utf-8')

                code_native = module.main([
                    '--timeout-seconds', '10',
                    '--receipt', str(receipt_native),
                    '--', sys.executable, str(deadlock_script)
                ])
                self.assertEqual(125, code_native)
                data_native = json.loads(receipt_native.read_text(encoding='utf-8'))
                self.assertEqual('HUNG_CONFIRMED', data_native['result'])
                self.assertEqual(125, data_native['exit_code'])
                self.assertIn('wait_chain_cycle_detected', data_native['diagnostic_observations'][0]['details'])

    def test_b1_06_inconclusive_health_evidence_continues_to_deadline(self):
        """B1-06: Quiet child without cycle evidence marks inconclusive and continues."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'inconclusive.json'
            module.INACTIVITY_THRESHOLD = 0.2
            module.POLL_INTERVAL = 0.05
            module.SAMPLE_INTERVAL = 0.05
            module.DIAGNOSTIC_INTERVAL = 0.15
            module.DIAGNOSTIC_PROBE_OVERRIDE = lambda pid: (False, 'probes_unsupported_or_inconclusive')

            code = module.main([
                '--timeout-seconds', '5',
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'import time; time.sleep(0.6)'
            ])
            self.assertEqual(0, code)
            data = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('PASS', data['result'])
            self.assertGreaterEqual(len(data['diagnostic_observations']), 1)
            self.assertEqual('DIAGNOSTIC_INCONCLUSIVE', data['diagnostic_observations'][0]['diagnosis'])

    def test_b1_07_child_error_exit_code_preserved(self):
        """B1-07: Child writing error and exiting nonzero returns prompt COMMAND_FAILED."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'err7.json'
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--timeout-seconds', '5',
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'import sys; sys.stderr.write("failure code 7\\n"); sys.exit(7)'
            ], capture_output=True, text=True, timeout=10)
            self.assertEqual(7, p.returncode, p.stderr)
            self.assertIn('[bounded] COMMAND_FAILED exit=7', p.stdout)
            self.assertNotIn('failure code 7', p.stdout)
            self.assertNotIn('failure code 7', p.stderr)

            receipt = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('COMMAND_FAILED', receipt['result'])
            self.assertEqual(7, receipt['exit_code'])
            stderr_file = Path(receipt['stderr_path'])
            self.assertIn('failure code 7', stderr_file.read_text(encoding='utf-8'))

    def test_b1_08_owned_descendants_killed_sentinel_survives(self):
        """B1-08: Parent -> child -> grandchild times out; sentinel survives (Windows native check)."""
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            pidfile = root / 'pids.txt'
            receiptfile = root / 'receipt.json'
            script = root / 'descendants.py'
            script.write_text('\n'.join([
                'import os, subprocess, sys, time',
                'from pathlib import Path',
                'role, pidfile = sys.argv[1:]',
                'with Path(pidfile).open("a", encoding="utf-8") as out:',
                '    out.write(f"{role} {os.getpid()}\\n")',
                'if role == "grandchild":',
                '    time.sleep(60)',
                'else:',
                '    next_role = "child" if role == "parent" else "grandchild"',
                '    subprocess.Popen([sys.executable, __file__, next_role, pidfile]).wait()',
            ]) + '\n', encoding='utf-8')
            sentinel = subprocess.Popen([sys.executable, '-c', 'import time;time.sleep(30)'],
                                        stdin=subprocess.DEVNULL,
                                        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            try:
                p = subprocess.run([sys.executable, str(WRAPPER), '--timeout-seconds', '4',
                                    '--receipt', str(receiptfile), '--', sys.executable,
                                    str(script), 'parent', str(pidfile)],
                                   capture_output=True, text=True, timeout=15)
                self.assertEqual(124, p.returncode, p.stderr)
                receipt = json.loads(receiptfile.read_text(encoding='utf-8'))
                self.assertEqual('TIMEOUT', receipt['result'])
                if os.name == 'nt':
                    self.assertIn('tree', receipt['cleanup'].lower())
                self.assertTrue(pidfile.exists(), 'Descendants did not start before timeout')
                ids = {}
                for line in pidfile.read_text(encoding='utf-8').splitlines():
                    role, pid = line.split()
                    ids[role] = int(pid)
                self.assertEqual({'parent', 'child', 'grandchild'}, set(ids))
                deadline = time.monotonic() + 3
                alive = {}
                while time.monotonic() < deadline:
                    alive = {role: pid for role, pid in ids.items() if process_running(pid)}
                    if not alive:
                        break
                    time.sleep(0.1)
                self.assertEqual({}, alive, f'Owned descendants survived timeout: {alive}')
                self.assertIsNone(sentinel.poll(), 'Unrelated sentinel was terminated')
            finally:
                sentinel.terminate()
                try:
                    sentinel.wait(timeout=4)
                except subprocess.TimeoutExpired:
                    sentinel.kill()
                    sentinel.wait(timeout=4)

    def test_b1_09_cancellation_and_launch_error(self):
        """B1-09: Cancellation/KeyboardInterrupt and launch errors produce safe non-PASS receipts."""
        with tempfile.TemporaryDirectory() as tmp:
            # 1. Launch error
            receipt_launch = Path(tmp) / 'launch_err.json'
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(receipt_launch),
                '--', 'nonexistent_cmd_xyz_123'
            ], capture_output=True, text=True, timeout=10)
            self.assertEqual(127, p.returncode)
            receipt = json.loads(receipt_launch.read_text(encoding='utf-8'))
            self.assertEqual('LAUNCH_ERROR', receipt['result'])
            self.assertEqual(127, receipt['exit_code'])

            # 2. KeyboardInterrupt / cancellation
            receipt_cancel = Path(tmp) / 'cancel.json'
            module.POLL_INTERVAL = 0.05
            orig_sleep = time.sleep
            calls = 0

            def mock_sleep(d):
                nonlocal calls
                calls += 1
                if calls >= 2:
                    raise KeyboardInterrupt()
                orig_sleep(d)

            with patch('time.sleep', side_effect=mock_sleep):
                code = module.main([
                    '--timeout-seconds', '10',
                    '--receipt', str(receipt_cancel),
                    '--', sys.executable, '-c', 'import time; time.sleep(10)'
                ])
            self.assertEqual(130, code)
            cancel_data = json.loads(receipt_cancel.read_text(encoding='utf-8'))
            self.assertEqual('CANCELLED', cancel_data['result'])
            self.assertEqual(130, cancel_data['exit_code'])

    def test_b1_10_no_clobber_and_no_canary_leak(self):
        """B1-10: Refuse before launch if receipt exists; no sensitive canary leaked in receipt/console."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'existing.json'
            receipt_path.write_text('{"preserved": true}\n', encoding='utf-8')

            # Pre-existing receipt must fail before launch
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'pass'
            ], capture_output=True, text=True, timeout=10)
            self.assertNotEqual(0, p.returncode)
            self.assertIn('Receipt file already exists', p.stderr)
            # Ensure file was not modified or truncated
            self.assertEqual('{"preserved": true}\n', receipt_path.read_text(encoding='utf-8'))

            # Canary test: ensure secrets in arguments or environment are never leaked
            canary_receipt = Path(tmp) / 'canary.json'
            canary_token = 'SECRET_CANARY_VALUE_XYZ_98765'
            p2 = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(canary_receipt),
                '--', sys.executable, '-c', f'print("canary arg: {canary_token}")'
            ], capture_output=True, text=True, timeout=10)
            self.assertEqual(0, p2.returncode)
            receipt_content = canary_receipt.read_text(encoding='utf-8')
            self.assertNotIn(canary_token, receipt_content)
            self.assertNotIn(canary_token, p2.stdout)

    def test_b1_11_large_streaming_unicode_and_long_line(self):
        """B1-11: Stream multi-megabyte output, Unicode and long line chunkwise to file."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'large.json'
            child_code = (
                "import sys\n"
                "out = sys.stdout.buffer\n"
                "chunk = ('X' * 1000 + '✓' + '語' + '—').encode('utf-8')\n"
                "for _ in range(2000):\n"
                "    out.write(chunk)\n"
                "out.write(b'\\n')\n"
                "line = (b'Y' * 1024) + b'\\n'\n"
                "for _ in range(3000):\n"
                "    out.write(line)\n"
                "out.flush()\n"
            )
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--timeout-seconds', '15',
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', child_code
            ], capture_output=True, text=True, timeout=20)
            self.assertEqual(0, p.returncode, p.stderr)
            receipt = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertTrue(receipt['capture_complete'])
            self.assertGreater(receipt['stdout_bytes'], 5_000_000)
            stdout_file = Path(receipt['stdout_path'])
            self.assertEqual(stdout_file.stat().st_size, receipt['stdout_bytes'])

    def test_b1_12_outer_timeout_supervision(self):
        """B1-12: Invoker outer host supervision cleanly bounds and terminates child process tree."""
        # Launch a child with a short outer timeout and verify clean termination
        start = time.monotonic()
        proc = subprocess.Popen(
            [sys.executable, str(WRAPPER), '--timeout-seconds', '60', '--', sys.executable, '-c', 'import time; time.sleep(60)'],
            stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )
        outer_deadline = 1.0
        time.sleep(outer_deadline)
        # Outer supervisor terminates the wrapper tree
        cleanup = module.kill_owned_tree(proc)
        try:
            proc.wait(timeout=3)
        except subprocess.TimeoutExpired:
            proc.kill()
            proc.wait(timeout=3)
        finally:
            if proc.stdout:
                proc.stdout.close()
            if proc.stderr:
                proc.stderr.close()
        elapsed = time.monotonic() - start
        self.assertLess(elapsed, 10.0, f"Outer supervisor took too long: {elapsed}s")
        self.assertFalse(process_running(proc.pid))

    def test_b1_13_remedy_f01_concurrent_log_collision(self):
        """B1-F01 remedy: Exclusive log creation prevents truncating pre-existing evidence."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'collision.json'
            stdout_path = Path(tmp) / 'collision.stdout.log'
            original = b'PREEXISTING_EVIDENCE_SENTINEL_DO_NOT_TRUNCATE'
            stdout_path.write_bytes(original)

            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', 'print("OVERWRITE_ATTEMPT")'
            ], capture_output=True, text=True, timeout=10)

            self.assertNotEqual(0, p.returncode)
            self.assertEqual(original, stdout_path.read_bytes())
            self.assertFalse(receipt_path.exists())

            # Also verify open('xb') raises FileExistsError and runner returns exit 1 without clobbering
            receipt_path2 = Path(tmp) / 'race.json'
            real_open = open
            def fake_open(p, mode='r', *args, **kwargs):
                if 'xb' in mode and 'race' in str(p):
                    raise FileExistsError(f"Simulated concurrent creation: {p}")
                return real_open(p, mode, *args, **kwargs)

            with patch('builtins.open', side_effect=fake_open):
                code = module.main(['--receipt', str(receipt_path2), '--', sys.executable, '-c', 'pass'])
            self.assertEqual(1, code)

    def test_b1_14_remedy_f02_capture_failure_not_pass(self):
        """B1-F02 remedy: Injected capture failure prevents PASS receipt."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'cap_fail.json'
            def fail_capture(self):
                self.error = OSError('SYNTHETIC_SINK_FAILURE')
                self.stream.close()

            with patch.object(module.StreamCapture, 'run', fail_capture):
                code = module.main(['--receipt', str(receipt_path), '--', sys.executable, '-c', 'pass'])

            self.assertNotEqual(0, code)
            d = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertNotEqual('PASS', d['result'])
            self.assertFalse(d['capture_complete'])

    def test_b1_15_remedy_f02_initial_probe_exception_safe_cleanup(self):
        """B1-F02 remedy: Health probe exception does not escape or leak child."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'probe_exc.json'
            with patch.object(module, 'get_process_cpu_io', side_effect=RuntimeError('SYNTHETIC_PROBE_FAILURE')):
                code = module.main([
                    '--timeout-seconds', '2',
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', 'import time; time.sleep(0.5)'
                ])
            self.assertTrue(receipt_path.exists())
            d = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertTrue(d['cleanup_complete'])

    def test_b1_16_remedy_f02_root_exit_descendant_tree_termination(self):
        """B1-F02 remedy: Descendants whose root exited are detected and cleaned up."""
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            receipt_path = root / 'root_exit.json'
            pidfile = root / 'descendant.pid'
            child = 'import time; time.sleep(15)'
            parent = (
                f'import subprocess, sys; from pathlib import Path; '
                f'p = subprocess.Popen([sys.executable, "-c", {repr(child)}]); '
                f'Path(sys.argv[1]).write_text(str(p.pid))'
            )
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--timeout-seconds', '2',
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', parent, str(pidfile)
            ], capture_output=True, text=True, timeout=10)

            self.assertTrue(pidfile.exists())
            descendant_pid = int(pidfile.read_text().strip())
            time.sleep(0.2)
            self.assertFalse(process_running(descendant_pid), f"Descendant {descendant_pid} leaked!")
            d = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertTrue(d['cleanup_complete'])

    def test_b1_17_remedy_f03_nan_inf_timeout_rejected(self):
        """B1-F03 remedy: Non-finite timeout values (nan, inf, -inf) are rejected before launch."""
        for val in ['nan', 'inf', '-inf', 'NaN', 'Infinity']:
            with tempfile.TemporaryDirectory() as tmp:
                receipt_path = Path(tmp) / f'nan_{val}.json'
                marker_file = Path(tmp) / 'child_ran.txt'
                p = subprocess.run([
                    sys.executable, str(WRAPPER),
                    f'--timeout-seconds={val}',
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', f'from pathlib import Path; Path({repr(str(marker_file))}).write_text("ran")'
                ], capture_output=True, text=True, timeout=10)
                self.assertNotEqual(0, p.returncode)
                self.assertFalse(marker_file.exists(), f"Child was launched despite invalid timeout {val}")
                self.assertFalse(receipt_path.exists())
                self.assertIn('finite', p.stderr.lower())

    def test_b1_18_remedy_f04_slow_diagnostic_bounded_by_budget(self):
        """B1-F04 remedy: Slow diagnostic probe is bounded by diag_budget and does not overrun timeout."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'slow_diag.json'
            module.POLL_INTERVAL = 0.02
            module.SAMPLE_INTERVAL = 0.02
            module.INACTIVITY_THRESHOLD = 0.05
            module.DIAGNOSTIC_INTERVAL = 0.02
            module.DIAGNOSTIC_BUDGET = 0.05
            module.DIAGNOSTIC_PROBE_OVERRIDE = lambda pid: (time.sleep(0.5) or (False, 'SYNTHETIC_SLOW_INCONCLUSIVE'))
            try:
                start = time.monotonic()
                code = module.main([
                    '--timeout-seconds', '0.2',
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', 'import time; time.sleep(5)'
                ])
                elapsed = time.monotonic() - start
                self.assertLess(elapsed, 2.0, f"Slow diagnostic exceeded grace budget: {elapsed}s")
                d = json.loads(receipt_path.read_text(encoding='utf-8'))
                self.assertEqual('TIMEOUT', d['result'])
            finally:
                module.POLL_INTERVAL = 0.5
                module.SAMPLE_INTERVAL = 5.0
                module.INACTIVITY_THRESHOLD = 300.0
                module.DIAGNOSTIC_INTERVAL = 30.0
                module.DIAGNOSTIC_BUDGET = 5.0
                module.DIAGNOSTIC_PROBE_OVERRIDE = None

    def test_b1_19_remedy_f06_small_live_output_chunkwise_observed(self):
        """B1-F06 remedy: Small flushed output is immediately read and saved to disk while child is running."""
        with tempfile.TemporaryDirectory() as tmp:
            log_file = Path(tmp) / 'live.log'
            proc = subprocess.Popen(
                [sys.executable, '-u', '-c', 'import sys, time; sys.stdout.buffer.write(b"heartbeat\\n"); sys.stdout.buffer.flush(); time.sleep(1.0)'],
                stdout=subprocess.PIPE,
                stderr=subprocess.DEVNULL
            )
            cap = module.StreamCapture(proc.stdout, log_file)
            cap.start()
            try:
                time.sleep(0.3)
                self.assertTrue(proc.poll() is None, "Child should still be alive during live sample")
                self.assertGreater(cap.bytes_written, 0, "Capture should have read flushed bytes before child exits")
                self.assertTrue(log_file.exists())
                self.assertIn(b"heartbeat", log_file.read_bytes())
                proc.wait(timeout=3)
                cap.join(timeout=2)
            finally:
                if proc.poll() is None:
                    proc.kill()
                    proc.wait()

    def test_b1_20_remedy_f05_no_canary_leak_in_console(self):
        """B1-F05 remedy: Sensitive child stderr is persisted to stderr log file and never leaked to console."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'canary.json'
            canary = 'SYNTHETIC_B1_PRIVATE_OUTPUT_CANARY_74ac'
            p = subprocess.run([
                sys.executable, str(WRAPPER),
                '--receipt', str(receipt_path),
                '--', sys.executable, '-c', f'import sys; sys.stderr.write("{canary}\\n"); sys.exit(7)'
            ], capture_output=True, text=True, timeout=10)

            self.assertEqual(7, p.returncode)
            self.assertNotIn(canary, p.stdout)
            self.assertNotIn(canary, p.stderr)
            stderr_path = receipt_path.with_name(receipt_path.stem + '.stderr.log')
            self.assertTrue(stderr_path.exists())
            self.assertIn(canary, stderr_path.read_text(encoding='utf-8'))

    def test_b1_21_residual_f01_receipt_reserved_before_launch(self):
        """B1-F01 residual: Pre-allocating receipt before Popen ensures concurrent receipt claim aborts before child spawn."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'receipt-race.json'
            marker = Path(tmp) / 'marker.txt'
            claimed = False
            real_open = open

            def claim_between_checks(path, mode='r', *args, **kwargs):
                nonlocal claimed
                if not claimed and str(path).endswith('receipt-race.stdout.log') and mode == 'xb':
                    claimed = True
                    with real_open(receipt_path, 'x', encoding='utf-8') as f:
                        f.write('{"owner":"concurrent_creator"}\n')
                return real_open(path, mode, *args, **kwargs)

            with patch('builtins.open', side_effect=claim_between_checks):
                code = module.main([
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', f'from pathlib import Path; Path({str(marker)!r}).write_text("executed")'
                ])

            self.assertEqual(1, code)
            self.assertFalse(marker.exists(), "Child process must not execute if receipt was claimed")
            self.assertEqual('{"owner":"concurrent_creator"}\n', receipt_path.read_text(encoding='utf-8'))

    def test_b1_22_residual_f02_read_oserror_fails_capture(self):
        """B1-F02 residual: os.read OSError is treated as capture failure, not clean EOF."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'read-error.json'
            fds = set()
            orig_init = module.StreamCapture.__init__

            def remember_init(capture_self, stream, *args, **kwargs):
                orig_init(capture_self, stream, *args, **kwargs)
                fds.add(stream.fileno())

            real_read = os.read

            def failed_read(fd, count):
                if fd in fds:
                    raise OSError('SYNTHETIC_READ_FAILURE')
                return real_read(fd, count)

            real_popen = subprocess.Popen

            def completed_child(*args, **kwargs):
                p = real_popen(*args, **kwargs)
                p.wait(timeout=3)
                return p

            with patch.object(module.subprocess, 'Popen', side_effect=completed_child), \
                 patch.object(module.StreamCapture, '__init__', remember_init), \
                 patch.object(module.os, 'read', side_effect=failed_read):
                code = module.main([
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', 'print("EXPECTED_CHILD_OUTPUT")'
                ])

            self.assertEqual(1, code)
            d = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('COMMAND_FAILED', d['result'])
            self.assertFalse(d['capture_complete'])

    def test_b1_23_residual_f02_capture_setup_failure_cleans_child_and_receipt(self):
        """B1-F02 residual: StreamCapture setup exception inside lifecycle cleans up child and writes receipt."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'capture-start.json'
            owned = []
            real_popen = subprocess.Popen

            def track_spawn(*args, **kwargs):
                p = real_popen(*args, **kwargs)
                owned.append(p)
                return p

            with patch.object(module.subprocess, 'Popen', side_effect=track_spawn), \
                 patch.object(module.StreamCapture, 'start', side_effect=RuntimeError('SYNTHETIC_THREAD_START_FAILURE')):
                with self.assertRaises(RuntimeError):
                    module.main([
                        '--receipt', str(receipt_path),
                        '--', sys.executable, '-c', 'import time; time.sleep(8)'
                    ])

            self.assertTrue(len(owned) > 0)
            child = owned[0]
            self.assertIsNotNone(child.poll(), "Child must be reaped/terminated even on capture setup exception")
            self.assertTrue(receipt_path.exists(), "Receipt must be written on capture setup failure")

    def test_b1_24_residual_f04_timing_bounds_on_health_and_descendant_cleanup(self):
        """B1-F04 residual: Health observation and descendant cleanup are bounded by respective budgets."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_health = Path(tmp) / 'slow-health.json'
            orig_poll = module.POLL_INTERVAL
            orig_diag = module.DIAGNOSTIC_BUDGET
            orig_grace = module.TOTAL_GRACE_BUDGET
            try:
                module.POLL_INTERVAL = 0.01
                module.DIAGNOSTIC_BUDGET = 0.01
                module.DIAGNOSTIC_PROBE_OVERRIDE = lambda pid: (False, 'synthetic_inconclusive')

                def slow_health(pid):
                    time.sleep(0.4)
                    return None

                with patch.object(module, 'get_process_cpu_io', side_effect=slow_health):
                    code = module.main([
                        '--timeout-seconds', '0.15',
                        '--receipt', str(receipt_health),
                        '--', sys.executable, '-c', 'import time; time.sleep(3)'
                    ])
                self.assertEqual(124, code)
                d_health = json.loads(receipt_health.read_text(encoding='utf-8'))
                self.assertLess(d_health['execution_duration_seconds'], 0.25)

                receipt_cleanup = Path(tmp) / 'slow-cleanup.json'
                module.TOTAL_GRACE_BUDGET = 0.06
                with patch.object(module, 'find_owned_descendants', side_effect=[[987654321], []]), \
                     patch.object(module, 'kill_descendants', side_effect=lambda ids: (time.sleep(0.3) or True)):
                    code_cleanup = module.main([
                        '--receipt', str(receipt_cleanup),
                        '--', sys.executable, '-c', 'pass'
                    ])
                self.assertEqual(1, code_cleanup)
                d_cleanup = json.loads(receipt_cleanup.read_text(encoding='utf-8'))
                self.assertLess(d_cleanup['cleanup_duration_seconds'], 0.15)
                self.assertFalse(d_cleanup['cleanup_complete'])
            finally:
                module.POLL_INTERVAL = orig_poll
                module.DIAGNOSTIC_BUDGET = orig_diag
                module.TOTAL_GRACE_BUDGET = orig_grace
                module.DIAGNOSTIC_PROBE_OVERRIDE = None

    def test_b1_25_residual_f05_receipt_write_exception_no_canary_leak(self):
        """B1-F05 residual: Exception details in write_receipt never leak raw canary text to console."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'write-failure.json'
            canary = 'SYNTHETIC_PRIVATE_EXCEPTION_CANARY_RR1_48c'
            import io
            out = io.StringIO()
            err = io.StringIO()
            with patch.object(module, 'write_receipt', side_effect=OSError(canary)), \
                 patch('sys.stdout', out), patch('sys.stderr', err):
                code = module.main([
                    '--receipt', str(receipt_path),
                    '--', sys.executable, '-c', 'pass'
                ])
            self.assertEqual(1, code)
            self.assertNotIn(canary, out.getvalue() + err.getvalue())
            self.assertIn('OSError', err.getvalue())

    def test_b1_26_residual_f02_unavailable_descendants_not_pass(self):
        """B1-F02 residual: Unavailable descendant verification marks cleanup incomplete and returns non-PASS."""
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            receipt_path = root / 'unknown-cleanup.json'
            pidfile = root / 'unknown-child.pid'
            owned = []
            child = 'import time; time.sleep(8)'
            parent = (
                f'import subprocess, sys; from pathlib import Path; '
                f'p = subprocess.Popen([sys.executable, "-c", {repr(child)}], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL); '
                f'Path(sys.argv[1]).write_text(str(p.pid))'
            )
            real_popen = subprocess.Popen

            def track_spawn(*args, **kwargs):
                p = real_popen(*args, **kwargs)
                owned.append(p)
                return p

            try:
                with patch.object(module.subprocess, 'Popen', side_effect=track_spawn), \
                     patch.object(module, 'find_owned_descendants', side_effect=OSError('SYNTHETIC_LOOKUP_UNAVAILABLE')):
                    code = module.main([
                        '--receipt', str(receipt_path),
                        '--', sys.executable, '-c', parent, str(pidfile)
                    ])

                self.assertEqual(1, code)
                d = json.loads(receipt_path.read_text(encoding='utf-8'))
                self.assertEqual('COMMAND_FAILED', d['result'])
                self.assertFalse(d['cleanup_complete'])
                self.assertIn('descendant_verification_unavailable', d['cleanup'])
            finally:
                if pidfile.exists():
                    try:
                        p_id = int(pidfile.read_text().strip())
                        if module.process_running(p_id):
                            subprocess.run(['taskkill.exe', '/PID', str(p_id), '/T', '/F'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=5)
                    except Exception:
                        pass
                for p in owned:
                    if p.poll() is None:
                        subprocess.run(['taskkill.exe', '/PID', str(p.pid), '/T', '/F'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=5)
                        p.wait(timeout=5)

    @unittest.skipUnless(os.name == 'nt', 'Native Windows enumeration boundary')
    def test_b1_29_f02_process32first_failure_not_pass(self):
        self._check_native_enumeration_failure('Process32First')

    @unittest.skipUnless(os.name == 'nt', 'Native Windows enumeration boundary')
    def test_b1_30_f02_process32next_failure_not_pass(self):
        self._check_native_enumeration_failure('Process32Next')

    def _check_native_enumeration_failure(self, api: str) -> None:
        import ctypes

        real_win_dll = ctypes.WinDLL
        calls = []
        closed = []

        def fail(*args):
            calls.append(api)
            ctypes.set_last_error(error)
            return 0

        class Kernel32Proxy:
            def __init__(self, dll):
                self.dll = dll
                self.dll.CloseHandle.argtypes = [ctypes.c_void_p]
                self.dll.CloseHandle.restype = ctypes.c_int

            def close(self, handle):
                result = self.dll.CloseHandle(handle)
                closed.append((handle, result))
                # Closing must not replace the enumeration error being propagated.
                ctypes.set_last_error(0)
                return result

            def __getattr__(self, name):
                if name == api:
                    return fail
                if name == 'CloseHandle':
                    # A function permits the helper's ctypes signature assignment.
                    def close_handle(handle):
                        return self.close(handle)
                    return close_handle
                return getattr(self.dll, name)

        def win_dll(name, *args, **kwargs):
            dll = real_win_dll(name, *args, **kwargs)
            return Kernel32Proxy(dll) if name.lower() == 'kernel32' else dll

        for error in (5, 0, 18):  # Access denied, unknown failure, normal end.
            with self.subTest(api=api, error=error):
                calls.clear()
                closed.clear()
                with patch.object(ctypes, 'WinDLL', side_effect=win_dll):
                    if error == 18:  # ERROR_NO_MORE_FILES alone permits empty results.
                        self.assertEqual([], module.find_owned_descendants(-1))
                    else:
                        with self.assertRaises(OSError) as raised:
                            module.find_owned_descendants(-1)
                        self.assertEqual(error, raised.exception.errno)
                        self.assertIn(api, str(raised.exception))
                self.assertEqual([api], calls)
                self.assertEqual(1, len(closed))
                self.assertTrue(closed[0][1])
                if error == 18:
                    continue

                with tempfile.TemporaryDirectory() as tmp:
                    receipt_path = Path(tmp) / 'native-unavailable.json'
                    pidfile = Path(tmp) / 'owned-child.pid'
                    child = 'import time; time.sleep(30)'
                    parent = (
                        'import subprocess, sys; from pathlib import Path; '
                        f'p = subprocess.Popen([sys.executable, "-c", {child!r}], '
                        'stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL); '
                        'Path(sys.argv[1]).write_text(str(p.pid))'
                    )
                    try:
                        with patch.object(ctypes, 'WinDLL', side_effect=win_dll):
                            code = module.main([
                                '--receipt', str(receipt_path),
                                '--', sys.executable, '-c', parent, str(pidfile)
                            ])
                        data = json.loads(receipt_path.read_text(encoding='utf-8'))
                        self.assertEqual(1, code)
                        self.assertEqual('COMMAND_FAILED', data['result'])
                        self.assertFalse(data['cleanup_complete'])
                        self.assertIn('descendant_verification_unavailable', data['cleanup'])
                        self.assertTrue(data['capture_complete'])
                        self.assertEqual([api, api], calls)
                        self.assertTrue(process_running(int(pidfile.read_text())))
                    finally:
                        if pidfile.exists():
                            pid = int(pidfile.read_text())
                            if process_running(pid):
                                subprocess.run(
                                    ['taskkill.exe', '/PID', str(pid), '/T', '/F'],
                                    stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                                    timeout=5, check=True
                                )
                            self.assertFalse(process_running(pid))

    def test_b1_27_residual_f04_shared_post_deadline_pool(self):
        """B1-F04 residual: Final diagnostic and shutdown share one post-deadline grace pool."""
        with tempfile.TemporaryDirectory() as tmp:
            receipt_path = Path(tmp) / 'shared-budget.json'
            orig_poll = module.POLL_INTERVAL
            orig_diag = module.DIAGNOSTIC_BUDGET
            orig_grace = module.TOTAL_GRACE_BUDGET
            orig_probe = module.DIAGNOSTIC_PROBE_OVERRIDE
            try:
                module.POLL_INTERVAL = 0.01
                module.DIAGNOSTIC_BUDGET = 0.12
                module.TOTAL_GRACE_BUDGET = 0.20
                module.DIAGNOSTIC_PROBE_OVERRIDE = lambda pid: (time.sleep(0.12) or (False, 'SYNTHETIC_FINAL_SNAPSHOT'))
                real_kill = module.kill_owned_tree
                observed = {}
                started = time.monotonic()

                def bounded_cleanup(proc, budget=module.CLEANUP_BUDGET):
                    begin = time.monotonic()
                    observed['callee_budget_seconds'] = budget
                    time.sleep(min(0.16, max(0, budget - 0.01)))
                    observed['first_termination_request_seconds'] = time.monotonic() - started
                    observed['child_alive_at_request'] = proc.poll() is None
                    return real_kill(proc, budget=max(0.001, budget - (time.monotonic() - begin)))

                with patch.object(module, 'kill_owned_tree', side_effect=bounded_cleanup):
                    code = module.main([
                        '--timeout-seconds', '0.15',
                        '--receipt', str(receipt_path),
                        '--', sys.executable, '-c', 'import time; time.sleep(3)'
                    ])

                self.assertEqual(124, code)
                d = json.loads(receipt_path.read_text(encoding='utf-8'))
                self.assertLessEqual(observed['first_termination_request_seconds'], 0.36)
                self.assertLess(observed['callee_budget_seconds'], 0.10)
                self.assertEqual(0.15, d['execution_duration_seconds'])
                self.assertGreaterEqual(d['diagnostic_duration_seconds'], 0.10)
            finally:
                module.POLL_INTERVAL = orig_poll
                module.DIAGNOSTIC_BUDGET = orig_diag
                module.TOTAL_GRACE_BUDGET = orig_grace
                module.DIAGNOSTIC_PROBE_OVERRIDE = orig_probe

    def test_b1_28_residual_f05_cli_exception_boundary_no_traceback_or_canary(self):
        """B1-F05 residual: Unexpected monitor exception at CLI boundary exits 1 without traceback or canary in console."""
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cli = root / 'cli_fault_test.py'
            receipt_path = root / 'cli-startup.json'
            canary = 'SYNTHETIC_PRIVATE_CLI_EXCEPTION_TEST_48b'
            cli_source = (
                f'import runpy, sys, threading\n'
                f'from unittest.mock import patch\n'
                f'original = threading.Thread.start\n'
                f'def fault(self, *a, **k):\n'
                f'    if type(self).__name__ == "StreamCapture":\n'
                f'        raise RuntimeError({repr(canary)})\n'
                f'    return original(self, *a, **k)\n'
                f'sys.argv = [{repr(str(WRAPPER))}, "--receipt", {repr(str(receipt_path))}, "--", sys.executable, "-c", "import time; time.sleep(8)"]\n'
                f'with patch.object(threading.Thread, "start", fault):\n'
                f'    runpy.run_path({repr(str(WRAPPER))}, run_name="__main__")\n'
            )
            cli.write_text(cli_source, encoding='utf-8')
            p = subprocess.run([sys.executable, '-B', str(cli)], capture_output=True, text=True, timeout=30)
            self.assertEqual(1, p.returncode)
            self.assertNotIn(canary, p.stdout + p.stderr)
            self.assertNotIn('Traceback', p.stdout + p.stderr)
            self.assertIn('INTERNAL_ERROR', p.stderr)
            d = json.loads(receipt_path.read_text(encoding='utf-8'))
            self.assertEqual('INTERNAL_ERROR', d['result'])
            self.assertTrue(d['cleanup_complete'])


if __name__ == '__main__':
    unittest.main()
