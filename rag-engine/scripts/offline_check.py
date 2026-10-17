"""Real OS/network namespace checks; no mocked sockets or resolver."""

import hashlib
import json
import os
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from uuid import uuid4

results: dict[str, object] = {}
for label, family, target in [
    ("ipv4", socket.AF_INET, ("1.1.1.1", 443)),
    ("ipv6", socket.AF_INET6, ("2606:4700:4700::1111", 443)),
]:
    with socket.socket(family, socket.SOCK_STREAM) as probe:
        probe.settimeout(2)
        try:
            probe.connect(target)
        except OSError:
            results[label + "_egress_denied"] = True
        else:
            raise AssertionError("External route exists")
try:
    dns = subprocess.run(
        [sys.executable, "-c", "import socket; socket.getaddrinfo('example.com',443)"],
        capture_output=True,
        timeout=5,
        check=False,
    )
    assert dns.returncode != 0, "External DNS unexpectedly resolved"
except subprocess.TimeoutExpired:
    pass
results["dns_resolution_denied"] = True
os.environ["HTTPS_PROXY"] = "http://1.1.1.1:8080"
try:
    urllib.request.urlopen("https://example.com", timeout=2)
except (OSError, urllib.error.URLError):
    results["proxy_egress_denied"] = True
else:
    raise AssertionError("Proxy egress succeeded")
os.environ["PPV_RAG_SERVICE_KEY"] = "synthetic-offline-check-key-00000000"
os.environ["PPV_RAG_WORKER_DSN"] = "postgresql://rag_worker:synthetic@127.0.0.1:5432/vault"
worker = subprocess.Popen(
    [
        sys.executable,
        "-m",
        "uvicorn",
        "ppv_rag.main:app",
        "--host",
        "127.0.0.1",
        "--port",
        "8001",
        "--no-access-log",
    ],
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL,
)
# Explicitly bypass inherited proxies for allowed loopback verification.
client = urllib.request.build_opener(urllib.request.ProxyHandler({}))
base = "http://127.0.0.1:8001"
try:
    deadline = time.monotonic() + 8
    while time.monotonic() < deadline:
        try:
            with client.open(base + "/health", timeout=1) as response:
                assert response.status == 200
                break
        except OSError:
            time.sleep(0.05)
    else:
        raise AssertionError("Loopback worker did not start")
    results["loopback_worker_health"] = True
    job, attempt = str(uuid4()), str(uuid4())
    payload = '{"contract_version":"2.0","sources":[]}'
    request = urllib.request.Request(
        base + "/v1/worker/execute",
        data=json.dumps(
            {
                "contract_version": "2.0",
                "job_id": job,
                "attempt_id": attempt,
                "lease_token": "a" * 64,
                "stage_type": "SYNTHETIC_TEST",
                "payload": payload,
                "input_digest": hashlib.sha256(payload.encode()).hexdigest(),
                "scope_digest": "b" * 64,
            }
        ).encode(),
        headers={
            "Content-Type": "application/json",
            "X-RAG-Service-Key": os.environ["PPV_RAG_SERVICE_KEY"],
        },
    )
    with client.open(request, timeout=5) as response:
        result = json.load(response)
        assert result["status"] == "REJECTED"
        assert result.get("receipt") is None
    results["unavailable_database_fails_closed"] = True
    results["persistent_model_or_scratch_cache"] = any(Path("/app").rglob("*.safetensors"))
    assert not results["persistent_model_or_scratch_cache"]
    results["python"] = sys.version.split()[0]
    print(json.dumps(results, sort_keys=True))
finally:
    worker.terminate()
    try:
        worker.wait(timeout=3)
    except subprocess.TimeoutExpired:
        worker.kill()
        worker.wait(timeout=3)
