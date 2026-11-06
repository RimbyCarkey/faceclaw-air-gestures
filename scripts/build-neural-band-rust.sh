#!/usr/bin/env bash
# Build the pinned bridge without cargo-ndk. Requires Rust 1.99.1 and the Android NDK.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
: "${ANDROID_NDK_HOME:?Set ANDROID_NDK_HOME to the installed Android NDK}"
case "$(uname -s)" in Darwin) host=darwin-x86_64 ;; Linux) host=linux-x86_64 ;; *) echo 'Build on macOS or Linux.' >&2; exit 1 ;; esac
bin="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$host/bin"
export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER="$bin/aarch64-linux-android29-clang"
export CC_aarch64_linux_android="$CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER"
export AR_aarch64_linux_android="$bin/llvm-ar"
export RUSTFLAGS="${RUSTFLAGS:-} -Clink-arg=-Wl,-z,max-page-size=16384 -Clink-arg=-Wl,-z,common-page-size=16384"
cd "$root/vendor/air-gestures/rust"
cargo +1.99.1 build --locked --release --target aarch64-linux-android -p bridge
mkdir -p "$root/native/air-gestures/jniLibs/arm64-v8a"
cp target/aarch64-linux-android/release/libairgestures_bridge.so "$root/native/air-gestures/jniLibs/arm64-v8a/"
