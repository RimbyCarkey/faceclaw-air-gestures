#!/usr/bin/env python3
"""Check release feeds; optionally create a review branch and PR. Never auto-merge."""
import argparse, json, os, re, subprocess, urllib.request
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
def run(*args): return subprocess.check_output(args, cwd=ROOT, text=True).strip()
def api(url):
    headers = {"User-Agent": "faceclaw-neural-band-upstream-sync"}
    if url.startswith("https://api.github.com/") and os.environ.get("GH_TOKEN"):
        headers["Authorization"] = "Bearer " + os.environ["GH_TOKEN"]
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=30) as response:
        return json.load(response)
def main():
    parser = argparse.ArgumentParser(); parser.add_argument("--apply", action="store_true"); args = parser.parse_args()
    lock_path = ROOT / "upstreams.lock.json"; lock = json.loads(lock_path.read_text())
    face = api("https://api.github.com/repos/jimrandomh/faceclaw/releases/latest")["tag_name"]
    air = api("https://gitlab.com/api/v4/projects/86888806/releases?per_page=1")[0]["tag_name"]
    if not all(re.fullmatch(r"[A-Za-z0-9._-]+", tag) for tag in (face, air)): raise RuntimeError("Unexpected release tag")
    print(json.dumps({"faceclaw": face, "air_gestures": air}))
    if face == lock["faceclaw"]["release"] and air == lock["air_gestures"]["release"]:
        print("Both upstream release baselines are current."); return
    if not args.apply: return
    if run("git", "status", "--porcelain"): raise RuntimeError("Start from a clean checkout")
    repo = os.environ.get("GH_REPO", "RimbyCarkey/faceclaw-air-gestures")
    branch = "automation/upstream-" + face + "-" + air
    existing = run("git", "ls-remote", "origin", "refs/heads/" + branch)
    if existing:
        print("An update branch for these releases already exists: " + branch); return
    run("git", "checkout", "-b", branch)
    if face != lock["faceclaw"]["release"]:
        run("git", "fetch", lock["faceclaw"]["repository"], "refs/tags/" + face)
        sha = run("git", "rev-parse", "FETCH_HEAD^{commit}")
        run("git", "merge", "--no-edit", "--no-ff", sha)
        lock["faceclaw"].update(commit=sha, release=face)
    if air != lock["air_gestures"]["release"]:
        path = lock["air_gestures"]["path"]
        run("git", "-C", path, "fetch", "origin", "refs/tags/" + air)
        sha = run("git", "-C", path, "rev-parse", "FETCH_HEAD^{commit}")
        run("git", "-C", path, "checkout", "--detach", sha)
        run("git", "-C", path, "submodule", "update", "--init", "--recursive")
        lock["air_gestures"].update(commit=sha, release=air, band_core=run("git", "-C", path + "/rust/band-core", "rev-parse", "HEAD"))
    lock_path.write_text(json.dumps(lock, indent=2) + "\n")
    run("git", "add", "upstreams.lock.json", "vendor/air-gestures")
    run("git", "commit", "-m", "Update upstream release baselines: Faceclaw " + face + ", Air Gestures " + air)
    run("git", "-c", "credential.helper=", "-c", "credential.https://github.com.helper=!gh auth git-credential", "push", "origin", branch)
    review = "https://github.com/" + repo + "/compare/main..." + branch + "?expand=1"
    print("Review upstream changes and open a PR: " + review)
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a") as output: output.write("Upstream update branch ready. [Review and open a PR](" + review + "). A test/APK build was dispatched.\n")
    run("gh", "workflow", "run", "native-band-android.yml", "--repo", repo, "--ref", branch)
if __name__ == "__main__": main()
