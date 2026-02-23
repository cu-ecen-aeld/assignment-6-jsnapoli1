# Assignment 6 Yocto Build (AESD)

This repository contains the Yocto/Poky setup for building an AESD image for `qemuarm64`.
Compared to the base starter state, it now includes:

- A `poky` git submodule pinned to the `kirkstone` branch.
- A completed `aesd-assignments` recipe that fetches, builds, and installs `aesdsocket`.
- Image configuration that includes `aesd-assignments` and `openssh`.
- Build script improvements for shared Yocto download/sstate cache usage in containerized CI.

## Repository Layout

```text
assignment-6-jsnapoli1/
├── poky/                  # Yocto Poky submodule (kirkstone)
├── meta-aesd/             # Custom layer with AESD recipes
├── build.sh               # Build helper (init env, set MACHINE, add layer, bitbake)
├── runqemu.sh             # Boot helper for qemuarm64 image
└── plan-assignment6-part2.md
```

## What Changed Since Commit `d98e5b15f1e139cfdbf610f8b2a334ba8b4148e1`

1. Added `poky` as a submodule in `.gitmodules` (branch `kirkstone`).
2. Completed `meta-aesd/.../aesd-assignments_git.bb`:
   - `SRC_URI` now points to the public assignments repo over HTTPS.
   - `SRCREV` is pinned for reproducibility.
   - `TARGET_LDFLAGS` includes `-pthread -lrt`.
   - `do_install()` now installs:
     - `/usr/bin/aesdsocket`
     - `/etc/init.d/S99aesdsocket` (from `aesdsocket-start-stop`)
   - `update-rc.d` integration is enabled via:
     - `inherit update-rc.d`
     - `INITSCRIPT_NAME` / `INITSCRIPT_PARAMS`
3. Updated `core-image-aesd.bb` to include `aesd-assignments` in the final image.
4. Updated `build.sh` to optionally use `/yocto-shared/downloads` and `/yocto-shared/sstate-cache` when available.
5. Updated GitHub Actions workflow to:
   - Make the SSH agent step non-fatal.
   - Mount a shared host cache directory into Docker at `/yocto-shared`.
   - Make cleanup resilient with `ssh-add -D || true`.
6. Added `plan-assignment6-part2.md` documenting the implementation plan and rationale.

## Build

From repository root:

```bash
./build.sh
```

`build.sh` will:

- Initialize/update submodules (including `poky`).
- Source `poky/oe-init-build-env`.
- Ensure `MACHINE = "qemuarm64"` is set in `conf/local.conf`.
- Add `meta-aesd` layer if missing.
- Build with `bitbake core-image-aesd`.

## Run in QEMU

```bash
./runqemu.sh
```

The built image should include `openssh` and start `aesdsocket` via `/etc/init.d/S99aesdsocket`.

## CI Notes

The GitHub Actions workflow runs the build inside `cuaesd/aesd-autotest:24-assignment6-yocto` and mounts a shared cache directory to reduce repeated download/build costs.
