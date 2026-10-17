"""Fast smoke and owned process-tree timeout tests for the bounded command runner.

The descendant test must also be executed on the owner's Windows host. A POSIX pass
is NOT proof that Windows taskkill /T behaves correctly in their environment.
"""
from __future__ import annotations

import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import unittest

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
        # SYNCHRONIZE is enough for this process-lifetime probe.
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
        # A killed grandchild can briefly remain a zombie until its adopter reaps it.
        state = proc_stat.read_text().rsplit(')', 1)[1].strip().split()[0]
        return state != 'Z'
    return True


class BoundedCommandTests(unittest.TestCase):
    def test_profile_guard(self):
        self.assertTrue(module.is_full_suite(['mvn', '-ntp', 'clean', 'verify']))
        self.assertTrue(module.is_full_suite(['python', '-m', 'pytest', 'tests']))
        self.assertFalse(module.is_full_suite(['mvn', 'test']))
        self.assertFalse(module.is_full_suite(['mvn', '-Dtest=Foo', 'clean', 'verify']))
        self.assertFalse(module.is_full_suite(['pip', 'install', 'torch']))

    def test_success_receipt(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'ok.json'
            p = subprocess.run([sys.executable, str(WRAPPER), '--timeout-seconds', '3',
                                '--receipt', str(path), '--', sys.executable, '-c', 'pass'],
                               capture_output=True, text=True, timeout=9)
            self.assertEqual(0, p.returncode, p.stderr)
            receipt = json.loads(path.read_text())
            self.assertEqual('PASS', receipt['result'])
            self.assertNotIn('argv', receipt)

    def test_timeout_receipt(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'timeout.json'
            p = subprocess.run([sys.executable, str(WRAPPER), '--timeout-seconds', '0.2',
                                '--receipt', str(path), '--', sys.executable, '-c',
                                'import time;time.sleep(5)'], capture_output=True, text=True, timeout=9)
            self.assertEqual(124, p.returncode, p.stderr)
            self.assertEqual('TIMEOUT', json.loads(path.read_text())['result'])

    def test_reject_non_suite_extended_profile(self):
        p = subprocess.run([sys.executable, str(WRAPPER), '--profile', 'full-suite', '--',
                            sys.executable, '-c', 'pass'], capture_output=True, text=True, timeout=9)
        self.assertNotEqual(0, p.returncode)
        self.assertIn('full-test-suite', p.stderr)

    def test_timeout_terminates_owned_child_and_grandchild_only(self):
        """Integration regression: parent -> child -> grandchild; sentinel survives."""
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            pidfile = root / 'pids.txt'
            receiptfile = root / 'receipt.json'
            # Every descendant inherits the parent's process group on POSIX;
            # taskkill /T finds the native parent/child tree on Windows.
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
                p = subprocess.run([sys.executable, str(WRAPPER), '--timeout-seconds', '5',
                                    '--receipt', str(receiptfile), '--', sys.executable,
                                    str(script), 'parent', str(pidfile)],
                                   capture_output=True, text=True, timeout=17)
                self.assertEqual(124, p.returncode, p.stderr)
                receipt = json.loads(receiptfile.read_text())
                self.assertEqual('TIMEOUT', receipt['result'])
                if os.name == 'nt':
                    self.assertIn('tree', receipt['cleanup'].lower())
                self.assertTrue(pidfile.exists(), 'Descendants did not start before timeout')
                ids = {}
                for line in pidfile.read_text().splitlines():
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
                # Only clean up the independent sentinel we created, not arbitrary processes.
                sentinel.terminate()
                try:
                    sentinel.wait(timeout=4)
                except subprocess.TimeoutExpired:
                    sentinel.kill()
                    sentinel.wait(timeout=4)


if __name__ == '__main__':
    unittest.main()
