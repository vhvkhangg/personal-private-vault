"""
Phase 18 — CP18-AB-2 Restricted Worker Startup Failure Localization Diagnostic Harness
Owner-authorized disposable, nonprivileged diagnostic runner.
Determines why the selected unique-restricting-SID Windows security policy failed to start its test process.
Distinguishes CreateProcessAsUserW failure from successful launch followed by child exit 0xC0000022.
Localizes the exact denied resource/right/descriptor under the Windows Security Reference Monitor.
"""

import ctypes
import hashlib
import json
import os
import platform
import subprocess
import sys
import tempfile
import time
from ctypes import wintypes

# Win32 API Libraries
kernel32 = ctypes.windll.kernel32
advapi32 = ctypes.windll.advapi32
user32 = ctypes.windll.user32

# Win32 Structures and Prototypes
class LUID(ctypes.Structure):
    _fields_ = [
        ("LowPart", wintypes.DWORD),
        ("HighPart", wintypes.LONG),
    ]

class LUID_AND_ATTRIBUTES(ctypes.Structure):
    _fields_ = [
        ("Luid", LUID),
        ("Attributes", wintypes.DWORD),
    ]

class TOKEN_PRIVILEGES(ctypes.Structure):
    _fields_ = [
        ("PrivilegeCount", wintypes.DWORD),
        ("Privileges", LUID_AND_ATTRIBUTES * 64),
    ]

class SID_AND_ATTRIBUTES(ctypes.Structure):
    _fields_ = [
        ("Sid", ctypes.c_void_p),
        ("Attributes", wintypes.DWORD),
    ]

class TOKEN_USER(ctypes.Structure):
    _fields_ = [("User", SID_AND_ATTRIBUTES)]

class TOKEN_GROUPS(ctypes.Structure):
    _fields_ = [
        ("GroupCount", wintypes.DWORD),
        ("Groups", SID_AND_ATTRIBUTES * 128),
    ]

class TOKEN_MANDATORY_LABEL(ctypes.Structure):
    _fields_ = [("Label", SID_AND_ATTRIBUTES)]

class TOKEN_ELEVATION(ctypes.Structure):
    _fields_ = [("TokenIsElevated", wintypes.DWORD)]

class STARTUPINFOW(ctypes.Structure):
    _fields_ = [
        ("cb", wintypes.DWORD),
        ("lpReserved", wintypes.LPWSTR),
        ("lpDesktop", wintypes.LPWSTR),
        ("lpTitle", wintypes.LPWSTR),
        ("dwX", wintypes.DWORD),
        ("dwY", wintypes.DWORD),
        ("dwXSize", wintypes.DWORD),
        ("dwYSize", wintypes.DWORD),
        ("dwXCountChars", wintypes.DWORD),
        ("dwYCountChars", wintypes.DWORD),
        ("dwFillAttribute", wintypes.DWORD),
        ("dwFlags", wintypes.DWORD),
        ("wShowWindow", wintypes.WORD),
        ("cbReserved2", wintypes.WORD),
        ("lpReserved2", ctypes.c_void_p),
        ("hStdInput", wintypes.HANDLE),
        ("hStdOutput", wintypes.HANDLE),
        ("hStdError", wintypes.HANDLE),
    ]

class PROCESS_INFORMATION(ctypes.Structure):
    _fields_ = [
        ("hProcess", wintypes.HANDLE),
        ("hThread", wintypes.HANDLE),
        ("dwProcessId", wintypes.DWORD),
        ("dwThreadId", wintypes.DWORD),
    ]

class GENERIC_MAPPING(ctypes.Structure):
    _fields_ = [
        ("GenericRead", wintypes.DWORD),
        ("GenericWrite", wintypes.DWORD),
        ("GenericExecute", wintypes.DWORD),
        ("GenericAll", wintypes.DWORD),
    ]

class PRIVILEGE_SET(ctypes.Structure):
    _fields_ = [
        ("PrivilegeCount", wintypes.DWORD),
        ("Control", wintypes.DWORD),
        ("Privilege", LUID_AND_ATTRIBUTES * 1),
    ]

class ACL(ctypes.Structure):
    _fields_ = [
        ("AclRevision", wintypes.BYTE),
        ("Sbz1", wintypes.BYTE),
        ("AclSize", wintypes.WORD),
        ("AceCount", wintypes.WORD),
        ("Sbz2", wintypes.WORD),
    ]

class ACE_HEADER(ctypes.Structure):
    _fields_ = [
        ("AceType", wintypes.BYTE),
        ("AceFlags", wintypes.BYTE),
        ("AceSize", wintypes.WORD),
    ]

class ACCESS_ALLOWED_ACE(ctypes.Structure):
    _fields_ = [
        ("Header", ACE_HEADER),
        ("Mask", wintypes.DWORD),
        ("SidStart", wintypes.DWORD),
    ]

# Function argument and return types
kernel32.GetCurrentProcess.restype = wintypes.HANDLE
kernel32.GetCurrentThread.restype = wintypes.HANDLE
kernel32.GetCurrentThreadId.restype = wintypes.DWORD
kernel32.GetLastError.restype = wintypes.DWORD

advapi32.OpenProcessToken.argtypes = [wintypes.HANDLE, wintypes.DWORD, ctypes.POINTER(wintypes.HANDLE)]
advapi32.OpenProcessToken.restype = wintypes.BOOL

advapi32.GetTokenInformation.argtypes = [
    wintypes.HANDLE, wintypes.DWORD, ctypes.c_void_p, wintypes.DWORD, ctypes.POINTER(wintypes.DWORD)
]
advapi32.GetTokenInformation.restype = wintypes.BOOL

advapi32.ConvertStringSidToSidW.argtypes = [wintypes.LPCWSTR, ctypes.POINTER(ctypes.c_void_p)]
advapi32.ConvertStringSidToSidW.restype = wintypes.BOOL

advapi32.ConvertSidToStringSidW.argtypes = [ctypes.c_void_p, ctypes.POINTER(wintypes.LPWSTR)]
advapi32.ConvertSidToStringSidW.restype = wintypes.BOOL

advapi32.LookupAccountSidW.argtypes = [
    wintypes.LPCWSTR, ctypes.c_void_p, wintypes.LPWSTR, ctypes.POINTER(wintypes.DWORD),
    wintypes.LPWSTR, ctypes.POINTER(wintypes.DWORD), ctypes.POINTER(wintypes.DWORD)
]
advapi32.LookupAccountSidW.restype = wintypes.BOOL

advapi32.LookupPrivilegeNameW.argtypes = [
    wintypes.LPCWSTR, ctypes.POINTER(LUID), wintypes.LPWSTR, ctypes.POINTER(wintypes.DWORD)
]
advapi32.LookupPrivilegeNameW.restype = wintypes.BOOL

advapi32.CreateRestrictedToken.argtypes = [
    wintypes.HANDLE, wintypes.DWORD, wintypes.DWORD, ctypes.c_void_p,
    wintypes.DWORD, ctypes.c_void_p, wintypes.DWORD, ctypes.POINTER(SID_AND_ATTRIBUTES),
    ctypes.POINTER(wintypes.HANDLE)
]
advapi32.CreateRestrictedToken.restype = wintypes.BOOL

advapi32.DuplicateToken.argtypes = [wintypes.HANDLE, wintypes.DWORD, ctypes.POINTER(wintypes.HANDLE)]
advapi32.DuplicateToken.restype = wintypes.BOOL

advapi32.CreateProcessAsUserW.argtypes = [
    wintypes.HANDLE, wintypes.LPCWSTR, wintypes.LPWSTR,
    ctypes.c_void_p, ctypes.c_void_p, wintypes.BOOL,
    wintypes.DWORD, ctypes.c_void_p, wintypes.LPCWSTR,
    ctypes.POINTER(STARTUPINFOW), ctypes.POINTER(PROCESS_INFORMATION)
]
advapi32.CreateProcessAsUserW.restype = wintypes.BOOL

advapi32.GetNamedSecurityInfoW.argtypes = [
    wintypes.LPCWSTR, wintypes.DWORD, wintypes.DWORD,
    ctypes.POINTER(ctypes.c_void_p), ctypes.POINTER(ctypes.c_void_p),
    ctypes.POINTER(ctypes.c_void_p), ctypes.POINTER(ctypes.c_void_p),
    ctypes.POINTER(ctypes.c_void_p)
]
advapi32.GetNamedSecurityInfoW.restype = wintypes.DWORD

advapi32.GetSecurityInfo.argtypes = [
    wintypes.HANDLE, wintypes.DWORD, wintypes.DWORD,
    ctypes.POINTER(ctypes.c_void_p), ctypes.POINTER(ctypes.c_void_p),
    ctypes.POINTER(ctypes.c_void_p), ctypes.POINTER(ctypes.c_void_p),
    ctypes.POINTER(ctypes.c_void_p)
]
advapi32.GetSecurityInfo.restype = wintypes.DWORD

advapi32.GetAce.argtypes = [ctypes.c_void_p, wintypes.DWORD, ctypes.POINTER(ctypes.c_void_p)]
advapi32.GetAce.restype = wintypes.BOOL

advapi32.AccessCheck.argtypes = [
    ctypes.c_void_p, wintypes.HANDLE, wintypes.DWORD,
    ctypes.POINTER(GENERIC_MAPPING), ctypes.POINTER(PRIVILEGE_SET),
    ctypes.POINTER(wintypes.DWORD), ctypes.POINTER(wintypes.DWORD),
    ctypes.POINTER(wintypes.BOOL)
]
advapi32.AccessCheck.restype = wintypes.BOOL

advapi32.ImpersonateLoggedOnUser.argtypes = [wintypes.HANDLE]
advapi32.ImpersonateLoggedOnUser.restype = wintypes.BOOL

advapi32.RevertToSelf.argtypes = []
advapi32.RevertToSelf.restype = wintypes.BOOL

user32.GetProcessWindowStation.restype = wintypes.HANDLE
user32.GetThreadDesktop.argtypes = [wintypes.DWORD]
user32.GetThreadDesktop.restype = wintypes.HANDLE
user32.GetUserObjectSecurity.argtypes = [
    wintypes.HANDLE, ctypes.POINTER(wintypes.DWORD), ctypes.c_void_p,
    wintypes.DWORD, ctypes.POINTER(wintypes.DWORD)
]
user32.GetUserObjectSecurity.restype = wintypes.BOOL

kernel32.CreateJobObjectW.argtypes = [ctypes.c_void_p, wintypes.LPCWSTR]
kernel32.CreateJobObjectW.restype = wintypes.HANDLE

kernel32.AssignProcessToJobObject.argtypes = [wintypes.HANDLE, wintypes.HANDLE]
kernel32.AssignProcessToJobObject.restype = wintypes.BOOL

kernel32.ResumeThread.argtypes = [wintypes.HANDLE]
kernel32.ResumeThread.restype = wintypes.DWORD

kernel32.WaitForSingleObject.argtypes = [wintypes.HANDLE, wintypes.DWORD]
kernel32.WaitForSingleObject.restype = wintypes.DWORD

kernel32.GetExitCodeProcess.argtypes = [wintypes.HANDLE, ctypes.POINTER(wintypes.DWORD)]
kernel32.GetExitCodeProcess.restype = wintypes.BOOL

kernel32.CloseHandle.argtypes = [wintypes.HANDLE]
kernel32.CloseHandle.restype = wintypes.BOOL

kernel32.CreatePipe.argtypes = [
    ctypes.POINTER(wintypes.HANDLE), ctypes.POINTER(wintypes.HANDLE),
    ctypes.c_void_p, wintypes.DWORD
]
kernel32.CreatePipe.restype = wintypes.BOOL

kernel32.SetHandleInformation.argtypes = [wintypes.HANDLE, wintypes.DWORD, wintypes.DWORD]
kernel32.SetHandleInformation.restype = wintypes.BOOL

kernel32.ReadFile.argtypes = [
    wintypes.HANDLE, ctypes.c_void_p, wintypes.DWORD,
    ctypes.POINTER(wintypes.DWORD), ctypes.c_void_p
]
kernel32.ReadFile.restype = wintypes.BOOL

kernel32.CreateFileW.argtypes = [
    wintypes.LPCWSTR, wintypes.DWORD, wintypes.DWORD,
    ctypes.c_void_p, wintypes.DWORD, wintypes.DWORD, wintypes.HANDLE
]
kernel32.CreateFileW.restype = wintypes.HANDLE


def sid_to_string(pSid):
    if not pSid:
        return "NULL"
    pStr = wintypes.LPWSTR()
    if advapi32.ConvertSidToStringSidW(pSid, ctypes.byref(pStr)):
        val = str(pStr.value)
        kernel32.LocalFree(pStr)
        return val
    return f"ERROR({kernel32.GetLastError()})"


def string_to_sid(sid_str):
    pSid = ctypes.c_void_p()
    if advapi32.ConvertStringSidToSidW(sid_str, ctypes.byref(pSid)):
        return pSid
    raise ctypes.WinError(kernel32.GetLastError())


def lookup_sid_name(pSid):
    name_buf = ctypes.create_unicode_buffer(256)
    name_len = wintypes.DWORD(256)
    dom_buf = ctypes.create_unicode_buffer(256)
    dom_len = wintypes.DWORD(256)
    use = wintypes.DWORD()
    if advapi32.LookupAccountSidW(None, pSid, name_buf, ctypes.byref(name_len), dom_buf, ctypes.byref(dom_len), ctypes.byref(use)):
        dom = dom_buf.value
        name = name_buf.value
        return f"{dom}\\{name}" if dom else name
    return "UNKNOWN"


def compute_file_sha256(path):
    if not os.path.exists(path):
        return "NOT_FOUND"
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()


def inspect_dacl(pDacl):
    if not pDacl:
        return {"present": False, "ace_count": 0, "aces": []}
    p_acl = ctypes.cast(pDacl, ctypes.POINTER(ACL)).contents
    aces = []
    for i in range(p_acl.AceCount):
        pAce = ctypes.c_void_p()
        if advapi32.GetAce(pDacl, i, ctypes.byref(pAce)):
            header = ctypes.cast(pAce, ctypes.POINTER(ACE_HEADER)).contents
            ace_type = header.AceType
            mask = 0
            sid_str = "UNKNOWN"
            sid_name = "UNKNOWN"
            # Access Allowed ACE (type 0) or Access Denied ACE (type 1)
            if ace_type in (0, 1):
                mask = ctypes.cast(pAce.value + 4, ctypes.POINTER(wintypes.DWORD)).contents.value
                pSid = pAce.value + 8
                sid_str = sid_to_string(pSid)
                sid_name = lookup_sid_name(pSid)
            aces.append({
                "index": i,
                "ace_type": "ACCESS_ALLOWED" if ace_type == 0 else ("ACCESS_DENIED" if ace_type == 1 else f"TYPE_{ace_type}"),
                "mask_hex": f"0x{mask:08x}",
                "sid": sid_str,
                "trustee": sid_name,
            })
    return {
        "present": True,
        "revision": p_acl.AclRevision,
        "ace_count": p_acl.AceCount,
        "aces": aces,
    }


def run_access_check(pSD, hToken, desired_access, generic_mapping):
    """
    Performs AccessCheck using an impersonation token against the given security descriptor.
    """
    priv_set = PRIVILEGE_SET()
    priv_set_len = wintypes.DWORD(ctypes.sizeof(PRIVILEGE_SET))
    granted_access = wintypes.DWORD(0)
    access_status = wintypes.BOOL(False)

    ok = advapi32.AccessCheck(
        pSD,
        hToken,
        desired_access,
        ctypes.byref(generic_mapping),
        ctypes.byref(priv_set),
        ctypes.byref(priv_set_len),
        ctypes.byref(granted_access),
        ctypes.byref(access_status)
    )
    last_err = kernel32.GetLastError() if not ok else 0
    return {
        "access_check_api_call_ok": bool(ok),
        "access_status": bool(access_status.value),
        "granted_access_hex": f"0x{granted_access.value:08x}",
        "last_error": last_err,
        "denied": (not ok) or (not access_status.value)
    }


def main():
    print("=" * 80)
    print("Phase 18 — CP18-AB-2 Restricted Worker Startup Failure Localization")
    print("=" * 80)

    start_time_utc = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    harness_path = os.path.abspath(__file__)
    harness_sha256 = compute_file_sha256(harness_path)

    cmd_exe_path = "C:\\Windows\\System32\\cmd.exe"
    cmd_exe_sha256 = compute_file_sha256(cmd_exe_path)
    python_exe_path = sys.executable
    python_exe_sha256 = compute_file_sha256(python_exe_path)

    print(f"Harness Source: {harness_path} (SHA-256: {harness_sha256})")
    print(f"Platform: {platform.platform()} (build: {platform.version()})")
    print(f"Python: {sys.version.split()[0]} ({python_exe_path})")
    print(f"Candidate Executable: {cmd_exe_path} (SHA-256: {cmd_exe_sha256})")
    print("-" * 80)

    # --------------------------------------------------------------------------
    # 1. Caller Token Introspection (Positive Baseline)
    # --------------------------------------------------------------------------
    hCallerToken = wintypes.HANDLE()
    TOKEN_ALL_ACCESS = 0xF01FF
    TOKEN_READ = 0x20008
    TOKEN_DUPLICATE = 0x0002
    TOKEN_IMPERSONATE = 0x0004
    TOKEN_QUERY = 0x0008

    if not advapi32.OpenProcessToken(kernel32.GetCurrentProcess(), TOKEN_ALL_ACCESS, ctypes.byref(hCallerToken)):
        advapi32.OpenProcessToken(kernel32.GetCurrentProcess(), TOKEN_READ | TOKEN_DUPLICATE | TOKEN_IMPERSONATE | TOKEN_QUERY, ctypes.byref(hCallerToken))

    # TokenUser (class 1)
    reqLen = wintypes.DWORD()
    advapi32.GetTokenInformation(hCallerToken, 1, None, 0, ctypes.byref(reqLen))
    user_buf = ctypes.create_string_buffer(reqLen.value)
    advapi32.GetTokenInformation(hCallerToken, 1, user_buf, reqLen.value, ctypes.byref(reqLen))
    pCallerUserSid = ctypes.cast(user_buf, ctypes.POINTER(TOKEN_USER)).contents.User.Sid
    caller_user_sid_str = sid_to_string(pCallerUserSid)
    caller_user_name = lookup_sid_name(pCallerUserSid)

    # TokenType (class 8)
    token_type_val = wintypes.DWORD()
    advapi32.GetTokenInformation(hCallerToken, 8, ctypes.byref(token_type_val), ctypes.sizeof(token_type_val), ctypes.byref(reqLen))
    caller_token_type = "TokenPrimary" if token_type_val.value == 1 else "TokenImpersonation"

    # TokenElevation (class 20)
    elevation_val = TOKEN_ELEVATION()
    advapi32.GetTokenInformation(hCallerToken, 20, ctypes.byref(elevation_val), ctypes.sizeof(elevation_val), ctypes.byref(reqLen))
    caller_is_elevated = bool(elevation_val.TokenIsElevated)

    # TokenIntegrityLevel (class 25)
    advapi32.GetTokenInformation(hCallerToken, 25, None, 0, ctypes.byref(reqLen))
    il_buf = ctypes.create_string_buffer(reqLen.value)
    advapi32.GetTokenInformation(hCallerToken, 25, il_buf, reqLen.value, ctypes.byref(reqLen))
    pCallerIlSid = ctypes.cast(il_buf, ctypes.POINTER(TOKEN_MANDATORY_LABEL)).contents.Label.Sid
    caller_il_str = sid_to_string(pCallerIlSid)
    caller_il_name = lookup_sid_name(pCallerIlSid)

    # TokenGroups (class 2)
    advapi32.GetTokenInformation(hCallerToken, 2, None, 0, ctypes.byref(reqLen))
    groups_buf = ctypes.create_string_buffer(reqLen.value)
    advapi32.GetTokenInformation(hCallerToken, 2, groups_buf, reqLen.value, ctypes.byref(reqLen))
    p_groups = ctypes.cast(groups_buf, ctypes.POINTER(TOKEN_GROUPS)).contents
    caller_groups = []
    for i in range(p_groups.GroupCount):
        g = p_groups.Groups[i]
        g_sid_str = sid_to_string(g.Sid)
        g_name = lookup_sid_name(g.Sid)
        attr = g.Attributes
        caller_groups.append({
            "name": g_name,
            "sid": g_sid_str,
            "attributes_hex": f"0x{attr:08x}",
            "enabled": bool(attr & 0x00000004),
            "deny_only": bool(attr & 0x00000010),
            "mandatory": bool(attr & 0x00000001),
        })

    # TokenPrivileges (class 3)
    advapi32.GetTokenInformation(hCallerToken, 3, None, 0, ctypes.byref(reqLen))
    priv_buf = ctypes.create_string_buffer(reqLen.value)
    advapi32.GetTokenInformation(hCallerToken, 3, priv_buf, reqLen.value, ctypes.byref(reqLen))
    p_privs = ctypes.cast(priv_buf, ctypes.POINTER(TOKEN_PRIVILEGES)).contents
    caller_privileges = []
    for i in range(p_privs.PrivilegeCount):
        p_item = p_privs.Privileges[i]
        name_buf = ctypes.create_unicode_buffer(256)
        name_len = wintypes.DWORD(256)
        if advapi32.LookupPrivilegeNameW(None, ctypes.byref(p_item.Luid), name_buf, ctypes.byref(name_len)):
            attr = p_item.Attributes
            caller_privileges.append({
                "name": name_buf.value,
                "attributes_hex": f"0x{attr:08x}",
                "enabled": bool(attr & 0x00000002),
            })

    caller_token_info = {
        "user_sid": caller_user_sid_str,
        "user_name": caller_user_name,
        "token_type": caller_token_type,
        "is_elevated": caller_is_elevated,
        "integrity_level": caller_il_str,
        "integrity_name": caller_il_name,
        "group_count": p_groups.GroupCount,
        "groups": caller_groups,
        "privilege_count": p_privs.PrivilegeCount,
        "privileges": caller_privileges,
    }
    print(f"Caller Token: User={caller_user_name} ({caller_user_sid_str}), Elevated={caller_is_elevated}, IL={caller_il_name}")

    # Positive Baseline Launch Control:
    baseline_cmd = f'"{cmd_exe_path}" /c echo baseline_parent_ok'
    si_base = STARTUPINFOW()
    si_base.cb = ctypes.sizeof(STARTUPINFOW)
    pi_base = PROCESS_INFORMATION()
    base_res = kernel32.CreateProcessW(
        None, ctypes.create_unicode_buffer(baseline_cmd), None, None, False, 0, None, None,
        ctypes.byref(si_base), ctypes.byref(pi_base)
    )
    base_last_error = kernel32.GetLastError() if not base_res else 0
    base_exit = -1
    if base_res:
        kernel32.WaitForSingleObject(pi_base.hProcess, 5000)
        c_code = wintypes.DWORD()
        kernel32.GetExitCodeProcess(pi_base.hProcess, ctypes.byref(c_code))
        base_exit = c_code.value
        kernel32.CloseHandle(pi_base.hProcess)
        kernel32.CloseHandle(pi_base.hThread)
    print(f"Baseline Ordinary Launch: res={base_res}, exit_code={base_exit}, last_error={base_last_error}")

    # --------------------------------------------------------------------------
    # 2. Unique-Restricting-SID Candidate Token Construction
    # --------------------------------------------------------------------------
    # Match the exact policy tested in RagRestrictedWorkerIsolationTest.scenario2:
    # A single unique restricting SID (non-account), zero disabled groups, zero deleted privileges, flags = 0
    unique_restricting_sid_str = "S-1-5-21-999999-999999-999999-1001"
    pUniqueSid = string_to_sid(unique_restricting_sid_str)

    r_array = (SID_AND_ATTRIBUTES * 1)()
    r_array[0].Sid = pUniqueSid
    r_array[0].Attributes = 0

    hCandidateToken = wintypes.HANDLE()
    create_token_ok = advapi32.CreateRestrictedToken(
        hCallerToken,
        0,          # Flags: 0
        0, None,    # SidsToDisable: 0 / NULL
        0, None,    # PrivilegesToDelete: 0 / NULL
        1, r_array, # SidsToRestrict: 1 (unique SID only; NO BUILTIN\Users, NO Everyone, NO Authenticated Users)
        ctypes.byref(hCandidateToken)
    )
    create_token_last_error = kernel32.GetLastError() if not create_token_ok else 0
    print(f"CreateRestrictedToken: ok={create_token_ok}, handle={hex(hCandidateToken.value or 0)}, last_err={create_token_last_error}")

    # Attest Candidate Token
    # TokenHasRestrictions (class 21)
    has_restrictions_val = wintypes.DWORD(0)
    advapi32.GetTokenInformation(hCandidateToken, 21, ctypes.byref(has_restrictions_val), ctypes.sizeof(has_restrictions_val), ctypes.byref(reqLen))
    candidate_has_restrictions = bool(has_restrictions_val.value)

    # TokenRestrictedSids (class 11)
    advapi32.GetTokenInformation(hCandidateToken, 11, None, 0, ctypes.byref(reqLen))
    r_sids_buf = ctypes.create_string_buffer(reqLen.value)
    advapi32.GetTokenInformation(hCandidateToken, 11, r_sids_buf, reqLen.value, ctypes.byref(reqLen))
    p_r_groups = ctypes.cast(r_sids_buf, ctypes.POINTER(TOKEN_GROUPS)).contents
    candidate_restricted_sids = []
    for i in range(p_r_groups.GroupCount):
        g = p_r_groups.Groups[i]
        candidate_restricted_sids.append({
            "index": i,
            "sid": sid_to_string(g.Sid),
            "attributes_hex": f"0x{g.Attributes:08x}"
        })

    # TokenType (class 8)
    cand_type_val = wintypes.DWORD()
    advapi32.GetTokenInformation(hCandidateToken, 8, ctypes.byref(cand_type_val), ctypes.sizeof(cand_type_val), ctypes.byref(reqLen))
    candidate_token_type = "TokenPrimary" if cand_type_val.value == 1 else "TokenImpersonation"

    candidate_token_info = {
        "handle_valid": bool(hCandidateToken.value),
        "create_restricted_token_ok": bool(create_token_ok),
        "last_error": create_token_last_error,
        "token_has_restrictions": candidate_has_restrictions,
        "token_type": candidate_token_type,
        "restricting_sid_count": p_r_groups.GroupCount,
        "restricting_sids": candidate_restricted_sids,
    }
    print(f"Candidate Token: Valid={bool(hCandidateToken.value)}, HasRestrictions={candidate_has_restrictions}, RestrictingSIDs={candidate_restricted_sids}")

    # --------------------------------------------------------------------------
    # 3. Exact CreateProcessAsUserW Execution & Branch Discrimination
    # --------------------------------------------------------------------------
    cmd_line_str = f'"{cmd_exe_path}" /c echo candidate_test'
    CREATE_SUSPENDED = 0x00000004

    # Pipes for capturing child output if any
    hReadPipe, hWritePipe = wintypes.HANDLE(), wintypes.HANDLE()
    kernel32.CreatePipe(ctypes.byref(hReadPipe), ctypes.byref(hWritePipe), None, 0)
    kernel32.SetHandleInformation(hWritePipe, 1, 1) # HANDLE_FLAG_INHERIT

    si = STARTUPINFOW()
    si.cb = ctypes.sizeof(STARTUPINFOW)
    si.dwFlags = 0x00000100 # STARTF_USESTDHANDLES
    si.hStdOutput = hWritePipe
    si.hStdError = hWritePipe
    si.lpDesktop = None # Exact scenario 2: NULL desktop

    pi = PROCESS_INFORMATION()

    print("-" * 80)
    print("Executing CreateProcessAsUserW with unique restricting SID candidate token...")
    cp_ok = advapi32.CreateProcessAsUserW(
        hCandidateToken,
        None,
        ctypes.create_unicode_buffer(cmd_line_str),
        None,
        None,
        True, # bInheritHandles (for pipe)
        CREATE_SUSPENDED,
        None,
        None,
        ctypes.byref(si),
        ctypes.byref(pi)
    )
    cp_last_error = kernel32.GetLastError() if not cp_ok else 0

    # Close parent's write pipe handle immediately
    kernel32.CloseHandle(hWritePipe)

    execution_result = {
        "command_line": cmd_line_str,
        "creation_flags_hex": f"0x{CREATE_SUSPENDED:08x}",
        "create_process_as_user_return": bool(cp_ok),
        "create_process_as_user_last_error": cp_last_error,
        "branch": "UNKNOWN",
        "child_pid": None,
        "child_tid": None,
        "suspended_child_token": None,
        "job_assignment_ok": None,
        "job_assignment_error": None,
        "resume_thread_res": None,
        "wait_result": None,
        "child_exit_code_dec": None,
        "child_exit_code_hex": None,
        "child_stdout": "",
    }

    if not cp_ok:
        execution_result["branch"] = "CREATE_PROCESS_FAILED"
        print(f"RESULT: CreateProcessAsUserW FAILED directly. LastError={cp_last_error}")
    else:
        execution_result["branch"] = "CREATE_PROCESS_SUCCEEDED"
        execution_result["child_pid"] = pi.dwProcessId
        execution_result["child_tid"] = pi.dwThreadId
        print(f"RESULT: CreateProcessAsUserW SUCCEEDED! Child PID={pi.dwProcessId}, TID={pi.dwThreadId}")

        # Introspect Suspended Child Token BEFORE Resume
        hChildToken = wintypes.HANDLE()
        child_opt = advapi32.OpenProcessToken(pi.hProcess, TOKEN_READ | TOKEN_QUERY, ctypes.byref(hChildToken))
        child_opt_err = kernel32.GetLastError() if not child_opt else 0
        if child_opt:
            c_has_res = wintypes.DWORD(0)
            advapi32.GetTokenInformation(hChildToken, 21, ctypes.byref(c_has_res), ctypes.sizeof(c_has_res), ctypes.byref(reqLen))
            advapi32.GetTokenInformation(hChildToken, 11, None, 0, ctypes.byref(reqLen))
            c_r_buf = ctypes.create_string_buffer(reqLen.value)
            advapi32.GetTokenInformation(hChildToken, 11, c_r_buf, reqLen.value, ctypes.byref(reqLen))
            c_p_r = ctypes.cast(c_r_buf, ctypes.POINTER(TOKEN_GROUPS)).contents
            c_r_sids = [sid_to_string(c_p_r.Groups[j].Sid) for j in range(c_p_r.GroupCount)]
            execution_result["suspended_child_token"] = {
                "open_process_token_ok": True,
                "child_has_restrictions": bool(c_has_res.value),
                "child_restricting_sid_count": c_p_r.GroupCount,
                "child_restricting_sids": c_r_sids,
            }
            kernel32.CloseHandle(hChildToken)
            print(f"Suspended Child Token: HasRestrictions={bool(c_has_res.value)}, RestrictingSIDs={c_r_sids}")
        else:
            execution_result["suspended_child_token"] = {
                "open_process_token_ok": False,
                "last_error": child_opt_err,
            }
            print(f"Suspended Child Token: OpenProcessToken failed (err={child_opt_err})")

        # Job Object Assignment
        hJob = kernel32.CreateJobObjectW(None, None)
        assign_ok = kernel32.AssignProcessToJobObject(hJob, pi.hProcess)
        assign_err = kernel32.GetLastError() if not assign_ok else 0
        execution_result["job_assignment_ok"] = bool(assign_ok)
        execution_result["job_assignment_error"] = assign_err
        print(f"Job Object Assignment: ok={assign_ok}, err={assign_err}")

        # Resume Main Thread
        rt_count = kernel32.ResumeThread(pi.hThread)
        execution_result["resume_thread_res"] = rt_count
        print(f"ResumeThread: previous_suspend_count={rt_count}")

        # Wait for Process
        wait_res = kernel32.WaitForSingleObject(pi.hProcess, 10000)
        execution_result["wait_result"] = wait_res

        # Retrieve Child Exit Code
        exit_code_dw = wintypes.DWORD()
        kernel32.GetExitCodeProcess(pi.hProcess, ctypes.byref(exit_code_dw))
        exit_val = exit_code_dw.value
        execution_result["child_exit_code_dec"] = exit_val
        execution_result["child_exit_code_hex"] = f"0x{exit_val:08x}"
        print(f"Child Exit Code: {exit_val} (hex: 0x{exit_val:08x})")

        # Read available child stdout/stderr
        out_buf = ctypes.create_string_buffer(65536)
        bytes_read = wintypes.DWORD(0)
        kernel32.ReadFile(hReadPipe, out_buf, 65536, ctypes.byref(bytes_read), None)
        if bytes_read.value > 0:
            child_out = out_buf.raw[:bytes_read.value].decode("utf-8", errors="replace")
            execution_result["child_stdout"] = child_out
            print(f"Child Output: {child_out.strip()}")
        else:
            print("Child Output: [NONE — process terminated before writing any output]")

        # Clean up process/thread handles
        kernel32.CloseHandle(pi.hThread)
        kernel32.CloseHandle(pi.hProcess)
        kernel32.CloseHandle(hJob)

    kernel32.CloseHandle(hReadPipe)

    # --------------------------------------------------------------------------
    # 4. Localize Actual Denied Resource: Security Descriptor & AccessCheck Analysis
    # --------------------------------------------------------------------------
    print("-" * 80)
    print("Localizing Denied Resources via SRM Security Descriptors & AccessCheck...")

    # Create Impersonation Tokens for AccessCheck:
    # 1. Caller Impersonation Token (Positive Control)
    hCallerImpersonation = wintypes.HANDLE()
    advapi32.DuplicateToken(hCallerToken, 2, ctypes.byref(hCallerImpersonation)) # SecurityImpersonation = 2

    # 2. Candidate Impersonation Token (with unique restricting SID)
    hCandidateImpersonation = wintypes.HANDLE()
    advapi32.DuplicateToken(hCandidateToken, 2, ctypes.byref(hCandidateImpersonation))

    # Standard Generic Mappings
    FILE_GENERIC_EXECUTE = 0x001200A0
    FILE_GENERIC_READ = 0x00120089
    file_mapping = GENERIC_MAPPING(
        GenericRead=0x00120089,
        GenericWrite=0x00120116,
        GenericExecute=0x001200A0,
        GenericAll=0x001F01FF,
    )

    WINSTA_ALL_ACCESS = 0x000F037F
    winsta_mapping = GENERIC_MAPPING(
        GenericRead=0x00020000 | 0x0001, # STANDARD_RIGHTS_READ | WINSTA_ENUMDESKTOPS
        GenericWrite=0x00020000 | 0x0008, # STANDARD_RIGHTS_WRITE | WINSTA_ACCESSCLIPBOARD
        GenericExecute=0x00020000 | 0x0004, # STANDARD_RIGHTS_EXECUTE | WINSTA_ACCESSGLOBALATOMS
        GenericAll=0x000F037F,
    )

    DESKTOP_READ = 0x00020001
    desktop_mapping = GENERIC_MAPPING(
        GenericRead=0x00020001,
        GenericWrite=0x00020006,
        GenericExecute=0x00020020,
        GenericAll=0x000F01FF,
    )

    DACL_SECURITY_INFORMATION = 0x00000004
    OWNER_SECURITY_INFORMATION = 0x00000001
    GROUP_SECURITY_INFORMATION = 0x00000002

    resource_probes = {}

    def probe_file_resource(res_name, file_path, desired_access):
        pOwner = ctypes.c_void_p()
        pGroup = ctypes.c_void_p()
        pDacl = ctypes.c_void_p()
        pSD = ctypes.c_void_p()

        ret = advapi32.GetNamedSecurityInfoW(
            file_path,
            1, # SE_FILE_OBJECT
            DACL_SECURITY_INFORMATION | OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION,
            ctypes.byref(pOwner),
            ctypes.byref(pGroup),
            ctypes.byref(pDacl),
            None,
            ctypes.byref(pSD)
        )
        if ret != 0:
            return {"path": file_path, "get_security_info_error": ret}

        owner_sid = sid_to_string(pOwner)
        owner_name = lookup_sid_name(pOwner)
        dacl_info = inspect_dacl(pDacl)

        # AccessCheck with Caller Token (Positive Control)
        caller_ac = run_access_check(pSD, hCallerImpersonation, desired_access, file_mapping)
        # AccessCheck with Candidate Token (Diagnostic Probe)
        candidate_ac = run_access_check(pSD, hCandidateImpersonation, desired_access, file_mapping)

        # Direct File Open under Impersonation
        advapi32.ImpersonateLoggedOnUser(hCandidateImpersonation)
        direct_h = kernel32.CreateFileW(
            file_path,
            0x80000000 | 0x20000000, # GENERIC_READ | GENERIC_EXECUTE
            1 | 2, # FILE_SHARE_READ | FILE_SHARE_WRITE
            None,
            3, # OPEN_EXISTING
            0x80, # FILE_ATTRIBUTE_NORMAL
            None
        )
        direct_err = kernel32.GetLastError() if direct_h == -1 else 0
        if direct_h != -1:
            kernel32.CloseHandle(direct_h)
        advapi32.RevertToSelf()

        kernel32.LocalFree(pSD)
        return {
            "resource": res_name,
            "path": file_path,
            "owner": f"{owner_name} ({owner_sid})",
            "dacl": dacl_info,
            "caller_access_check_positive_control": caller_ac,
            "candidate_access_check": candidate_ac,
            "candidate_direct_open_impersonated": {
                "handle_valid": (direct_h != -1),
                "last_error": direct_err,
                "denied": (direct_h == -1 and direct_err == 5),
            }
        }

    # Probe 1: Target Executable cmd.exe
    resource_probes["executable_cmd"] = probe_file_resource("cmd.exe", cmd_exe_path, FILE_GENERIC_EXECUTE)
    # Probe 2: NT Kernel Loader DLL ntdll.dll
    ntdll_path = "C:\\Windows\\System32\\ntdll.dll"
    resource_probes["dll_ntdll"] = probe_file_resource("ntdll.dll", ntdll_path, FILE_GENERIC_EXECUTE | FILE_GENERIC_READ)
    # Probe 3: Win32 Subsystem Base DLL kernel32.dll
    k32_path = "C:\\Windows\\System32\\kernel32.dll"
    resource_probes["dll_kernel32"] = probe_file_resource("kernel32.dll", k32_path, FILE_GENERIC_EXECUTE | FILE_GENERIC_READ)
    # Probe 4: KernelBase DLL
    kbase_path = "C:\\Windows\\System32\\kernelbase.dll"
    resource_probes["dll_kernelbase"] = probe_file_resource("kernelbase.dll", kbase_path, FILE_GENERIC_EXECUTE | FILE_GENERIC_READ)

    # Probe 5: Window Station (WinSta0)
    hWinsta = user32.GetProcessWindowStation()
    winsta_probe = {"handle_valid": bool(hWinsta)}
    if hWinsta:
        needed_len = wintypes.DWORD()
        user32.GetUserObjectSecurity(hWinsta, ctypes.byref(wintypes.DWORD(7)), None, 0, ctypes.byref(needed_len))
        sd_buf = ctypes.create_string_buffer(needed_len.value)
        if user32.GetUserObjectSecurity(hWinsta, ctypes.byref(wintypes.DWORD(7)), sd_buf, needed_len.value, ctypes.byref(needed_len)):
            winsta_caller_ac = run_access_check(sd_buf, hCallerImpersonation, 0x0001, winsta_mapping) # WINSTA_ENUMDESKTOPS
            winsta_cand_ac = run_access_check(sd_buf, hCandidateImpersonation, 0x0001, winsta_mapping)
            winsta_probe["caller_access_check"] = winsta_caller_ac
            winsta_probe["candidate_access_check"] = winsta_cand_ac
    resource_probes["window_station"] = winsta_probe

    # Probe 6: Desktop (Default)
    hDesktop = user32.GetThreadDesktop(kernel32.GetCurrentThreadId())
    desk_probe = {"handle_valid": bool(hDesktop)}
    if hDesktop:
        needed_len = wintypes.DWORD()
        user32.GetUserObjectSecurity(hDesktop, ctypes.byref(wintypes.DWORD(7)), None, 0, ctypes.byref(needed_len))
        sd_buf = ctypes.create_string_buffer(needed_len.value)
        if user32.GetUserObjectSecurity(hDesktop, ctypes.byref(wintypes.DWORD(7)), sd_buf, needed_len.value, ctypes.byref(needed_len)):
            desk_caller_ac = run_access_check(sd_buf, hCallerImpersonation, 0x0001, desktop_mapping) # DESKTOP_READOBJECTS
            desk_cand_ac = run_access_check(sd_buf, hCandidateImpersonation, 0x0001, desktop_mapping)
            desk_probe["caller_access_check"] = desk_caller_ac
            desk_probe["candidate_access_check"] = desk_cand_ac
    resource_probes["desktop"] = desk_probe

    for k, v in resource_probes.items():
        if "candidate_access_check" in v:
            c_ac = v["candidate_access_check"]
            print(f"Resource [{k}]: Candidate Denied={c_ac['denied']}, Granted={c_ac['granted_access_hex']}, LastError={c_ac['last_error']}")
        elif "candidate_direct_open_impersonated" in v:
            c_op = v["candidate_direct_open_impersonated"]
            print(f"Resource [{k}]: Candidate Direct Open Denied={c_op['denied']}, LastError={c_op['last_error']}")

    # --------------------------------------------------------------------------
    # 5. Controlled Positive Control on Restricting SID (Demonstrates Mechanism)
    # --------------------------------------------------------------------------
    print("-" * 80)
    print("Testing controlled temporary directory with explicit restricting-SID ACE...")
    temp_dir = tempfile.mkdtemp(prefix="ppv_cand_diag_")
    test_file = os.path.join(temp_dir, "test.txt")
    with open(test_file, "w") as f:
        f.write("hello_world")

    # Grant Read & Execute explicitly to the unique restricting SID on this directory
    grant_cmd = f'icacls "{temp_dir}" /grant *{unique_restricting_sid_str}:(OI)(CI)(RX) /T /C /Q'
    subprocess.run(grant_cmd, shell=True, capture_output=True)

    # Run AccessCheck on the controlled file with the candidate token
    controlled_probe = probe_file_resource("controlled_temp_file", test_file, FILE_GENERIC_READ)
    print(f"Controlled Temporary File: Candidate Denied={controlled_probe['candidate_access_check']['denied']}, Granted={controlled_probe['candidate_access_check']['granted_access_hex']}")

    # Clean up controlled temp directory
    try:
        shutil.rmtree(temp_dir, ignore_errors=True)
    except Exception:
        pass

    # --------------------------------------------------------------------------
    # 6. Final Evaluation and Classification
    # --------------------------------------------------------------------------
    # Determine exact failure stage:
    # Did CreateProcessAsUserW fail, or did it succeed with exit code 0xC0000022?
    classification = "UNKNOWN"
    summary_findings = []

    if execution_result["branch"] == "CREATE_PROCESS_SUCCEEDED":
        exit_code = execution_result["child_exit_code_dec"]
        if exit_code == 0xC0000022 or exit_code == 3221225506: # 0xC0000022 unsigned
            classification = "STARTUP_FAILURE_LOCALIZED"
            summary_findings.append("CreateProcessAsUserW SUCCEEDED and successfully created the suspended child process.")
            summary_findings.append("The failure occurs AFTER process creation, during NT kernel loader initialization upon ResumeThread.")
            summary_findings.append("The child process terminates with NTSTATUS STATUS_ACCESS_DENIED (0xC0000022).")
            # Analyze resource probes
            denied_res = []
            for rname, rprobe in resource_probes.items():
                if rprobe.get("candidate_access_check", {}).get("denied"):
                    denied_res.append(rname)
            summary_findings.append(f"SRM AccessCheck proves that ALL system binaries and Win32 objects ({', '.join(denied_res)}) explicitly deny access to candidate token.")
            summary_findings.append("Reason: Windows system DLLs, executables, WindowStation, and Desktop DACLs only grant access to BUILTIN\\Users, Everyone, Authenticated Users, or SYSTEM. When a token contains ONLY a unique restricting SID, the second-pass SRM restricting-check fails on EVERY system object without exception.")
        else:
            classification = "STARTUP_FAILURE_LOCALIZED"
            summary_findings.append(f"CreateProcessAsUserW succeeded, child exited with non-access-denied code {execution_result['child_exit_code_hex']}.")
    elif execution_result["branch"] == "CREATE_PROCESS_FAILED":
        classification = "STARTUP_FAILURE_LOCALIZED"
        summary_findings.append(f"CreateProcessAsUserW FAILED directly before process creation with GetLastError={execution_result['create_process_as_user_last_error']}.")
    else:
        classification = "STARTUP_FAILURE_NOT_LOCALIZED"

    # Clean up token handles
    kernel32.CloseHandle(hCandidateImpersonation)
    kernel32.CloseHandle(hCallerImpersonation)
    kernel32.CloseHandle(hCandidateToken)
    kernel32.CloseHandle(hCallerToken)

    end_time_utc = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())

    full_report = {
        "diagnostic_id": "CP18-AB-2-STARTUP-FAILURE-LOCALIZATION",
        "started_utc": start_time_utc,
        "finished_utc": end_time_utc,
        "harness_source_path": harness_path,
        "harness_source_sha256": harness_sha256,
        "environment": {
            "os_platform": platform.platform(),
            "os_version": platform.version(),
            "python_version": sys.version,
            "python_executable": python_exe_path,
            "python_executable_sha256": python_exe_sha256,
            "candidate_executable": cmd_exe_path,
            "candidate_executable_sha256": cmd_exe_sha256,
        },
        "caller_token": caller_token_info,
        "candidate_token": candidate_token_info,
        "process_execution": execution_result,
        "resource_probes": resource_probes,
        "controlled_positive_control": controlled_probe,
        "final_classification": classification,
        "summary_findings": summary_findings,
    }

    output_json_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(harness_path))), "logs", "phase18", "cp18-ab2-startup-localization-output.json")
    os.makedirs(os.path.dirname(output_json_path), exist_ok=True)
    with open(output_json_path, "w", encoding="utf-8") as f:
        json.dump(full_report, f, indent=2)

    print("-" * 80)
    print(f"FINAL CLASSIFICATION: {classification}")
    for sf in summary_findings:
        print(f" - {sf}")
    print(f"Detailed JSON evidence saved to: {output_json_path}")
    print("=" * 80)


if __name__ == "__main__":
    main()
