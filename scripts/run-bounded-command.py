#!/usr/bin/env python3
"""Start one owned command with a hard wait deadline and best-effort tree cleanup."""
from __future__ import annotations
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import tempfile
import time

NORMAL_CAP=1200
FULL_CAP=2100

def is_full_suite(args: list[str]) -> bool:
    if not args: return False
    exe=Path(args[0]).name.lower()
    rest=[x.lower() for x in args[1:]]
    if exe in {'mvn','mvn.cmd','mvnw','mvnw.cmd','mvnw.bat'}:
        return ('clean' in rest and 'verify' in rest and not any(x.startswith(('-dtest=','-dskiptests','-dmaven.test.skip')) for x in rest))
    if exe in {'python','python.exe','python3','python3.exe','py','py.exe'}:
        return rest[:2]==['-m','pytest'] and not any(x=='-k' or x.startswith('--ignore') for x in rest)
    return exe in {'pytest','pytest.exe'} and not any(x=='-k' or x.startswith('--ignore') for x in rest)

def iso_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec='milliseconds')

def write_receipt(path: Path|None, data: dict) -> None:
    if path is None: return
    path.parent.mkdir(parents=True,exist_ok=True)
    fd,tmp=tempfile.mkstemp(prefix='.'+path.name+'.',suffix='.tmp',dir=path.parent)
    try:
        with os.fdopen(fd,'w',encoding='utf-8') as f:
            json.dump(data,f,indent=2);f.write('\n')
        os.replace(tmp,path)
    finally:
        if os.path.exists(tmp): os.unlink(tmp)

def kill_owned_tree(proc: subprocess.Popen) -> str:
    if proc.poll() is not None:return 'already_exited'
    if os.name=='nt':
        command=shutil.which('taskkill.exe') or shutil.which('taskkill')
        if command:
            try:
                result=subprocess.run([command,'/PID',str(proc.pid),'/T','/F'],stdin=subprocess.DEVNULL,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=10,check=False)
                if result.returncode==0:return 'windows_tree_termination_requested'
            except (OSError,subprocess.TimeoutExpired):pass
        if proc.poll() is None:proc.kill()
        return 'windows_parent_killed_tree_unverified'
    try:
        os.killpg(proc.pid,signal.SIGKILL)
        return 'posix_process_group_killed'
    except ProcessLookupError:return 'already_exited'
    except OSError:
        if proc.poll() is None:proc.kill()
        return 'posix_parent_killed_tree_unverified'

def main(argv: list[str]|None=None)->int:
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile',choices=['normal','full-suite'],default='normal')
    parser.add_argument('--timeout-seconds',type=float)
    parser.add_argument('--receipt',type=Path)
    parser.add_argument('command',nargs=argparse.REMAINDER)
    args=parser.parse_args(argv)
    cmd=args.command[1:] if args.command[:1]==['--'] else args.command
    if not cmd:parser.error('Supply executable and arguments after --')
    cap=FULL_CAP if args.profile=='full-suite' else NORMAL_CAP
    if args.profile=='full-suite' and not is_full_suite(cmd):
        parser.error('Extended timeout requires an actual full-test-suite command')
    timeout=float(args.timeout_seconds) if args.timeout_seconds is not None else float(cap)
    if timeout<=0 or timeout>cap:parser.error(f'timeout must be >0 and <= {cap} seconds')
    t=time.monotonic()
    # Never write raw argv/environment to receipt: secrets could be present.
    data=dict(profile=args.profile,timeout_seconds=timeout,executable=Path(cmd[0]).name,
              argv_sha256=hashlib.sha256('\0'.join(cmd).encode()).hexdigest(),started_utc=iso_now())
    print(f'[bounded] starting {cmd[0]} (limit {timeout:g}s)',flush=True)
    try:
        proc=subprocess.Popen(cmd,stdin=subprocess.DEVNULL,start_new_session=os.name!='nt',
             creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name=='nt' else 0)
    except OSError as exc:
        print(f'[bounded] launch failed: {exc}',file=sys.stderr,flush=True)
        data.update(result='LAUNCH_ERROR',exit_code=127,finished_utc=iso_now())
        write_receipt(args.receipt,data)
        return 127
    cleanup='not_needed';timeout_hit=False
    try:code=proc.wait(timeout=timeout)
    except subprocess.TimeoutExpired:
        timeout_hit=True
        cleanup=kill_owned_tree(proc)
        try:proc.wait(timeout=10)
        except subprocess.TimeoutExpired:cleanup+=';unreaped_process'
        code=124
    data.update(pid=proc.pid,finished_utc=iso_now(),duration_seconds=round(time.monotonic()-t,3),
                result='TIMEOUT' if timeout_hit else ('PASS' if code==0 else 'COMMAND_FAILED'),
                exit_code=code,cleanup=cleanup)
    write_receipt(args.receipt,data)
    print(f"[bounded] {data['result']} exit={code} cleanup={cleanup}",flush=True)
    return 124 if timeout_hit else (code if 0<=code<=125 else 1)
if __name__=='__main__':raise SystemExit(main())
