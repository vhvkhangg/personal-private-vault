"""Run exactly the opt-in Java R7 diagnostic using the already verified test classpath."""

import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parents[2]
report = next(
    (root / "backend/target/surefire-reports").glob(
        "TEST-*RagNativeDocumentBatchIntegrationTest.xml"
    )
)
properties = ET.parse(report).getroot().find("properties")
assert properties is not None
classpath = next(p.attrib["value"] for p in properties if p.attrib["name"] == "java.class.path")
source = (
    root
    / "backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagR7FeasibilityCheckpoint.java"
)
raise SystemExit(
    subprocess.run(
        [
            "java",
            "--enable-native-access=ALL-UNNAMED",
            "--class-path",
            classpath,
            str(source),
            str(root / "logs/phase18-d18-pt-a"),
        ],
        timeout=240,
        check=False,
    ).returncode
)
