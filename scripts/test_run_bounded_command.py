"""Cross-platform quick smoke checks for the bounded command watchdog."""
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

WRAPPER=Path(__file__).with_name('run-bounded-command.py')
spec=importlib.util.spec_from_file_location('bounded',WRAPPER)
module=importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class BoundedCommandTests(unittest.TestCase):
    def test_profile_guard(self):
        self.assertTrue(module.is_full_suite(['mvn','-ntp','clean','verify']))
        self.assertTrue(module.is_full_suite(['python','-m','pytest','tests']))
        self.assertFalse(module.is_full_suite(['mvn','test']))
        self.assertFalse(module.is_full_suite(['mvn','-Dtest=Foo','clean','verify']))
        self.assertFalse(module.is_full_suite(['pip','install','torch']))
    def test_success_receipt(self):
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'ok.json'
            p=subprocess.run([sys.executable,str(WRAPPER),'--timeout-seconds','3','--receipt',str(path),'--',sys.executable,'-c','pass'],capture_output=True,text=True,timeout=9)
            self.assertEqual(0,p.returncode,p.stderr)
            d=json.loads(path.read_text())
            self.assertEqual('PASS',d['result'])
            self.assertNotIn('argv',d)
    def test_timeout_receipt(self):
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'timeout.json'
            p=subprocess.run([sys.executable,str(WRAPPER),'--timeout-seconds','0.12','--receipt',str(path),'--',sys.executable,'-c','import time;time.sleep(5)'],capture_output=True,text=True,timeout=9)
            self.assertEqual(124,p.returncode,p.stderr)
            self.assertEqual('TIMEOUT',json.loads(path.read_text())['result'])
    def test_reject_non_suite_extended_profile(self):
        p=subprocess.run([sys.executable,str(WRAPPER),'--profile','full-suite','--',sys.executable,'-c','pass'],capture_output=True,text=True,timeout=9)
        self.assertNotEqual(0,p.returncode)
        self.assertIn('full-test-suite',p.stderr)
if __name__=='__main__':unittest.main()
