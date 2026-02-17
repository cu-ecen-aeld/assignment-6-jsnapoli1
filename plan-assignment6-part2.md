# Plan: Assignment 6 Part 2 — Yocto/Poky Build

## Objective
Build a Yocto/Poky (kirkstone) image for qemuarm64 that includes the aesdsocket daemon
from the assignments-3-and-later repo. Verify it starts automatically and passes full-test.sh.

## Architecture
```
assignment-6-jsnapoli1/
├── poky/                  (git submodule, kirkstone branch)
├── meta-aesd/
│   └── recipes-aesd-assignments/
│       ├── aesd-assignments/
│       │   └── aesd-assignments_git.bb   ← updated recipe
│       └── images/
│           └── core-image-aesd.bb        ← add aesd-assignments package
├── build.sh               (sources poky/oe-init-build-env, runs bitbake)
└── runqemu.sh
```

## Pseudocode Blocks

### Block 1: Add poky submodule (kirkstone)
```
# Navigate to assignment-6 repo root
# Add git submodule: https://git.yoctoproject.org/poky, branch kirkstone
# Initialize and update submodule recursively
# Commit the submodule .gitmodules + pointer
```

### Block 2: Update aesd-assignments_git.bb recipe
```
# Set SRC_URI to assignments-3-and-later repo (https protocol, branch master)
# Set SRCREV to latest commit hash of Part 1 implementation
# Uncomment TARGET_LDFLAGS for -pthread (required by aesdsocket)
# Set FILES to install /usr/bin/aesdsocket and /etc/init.d/S99aesdsocket
# Update do_install:
#   - install -d ${D}${bindir}
#   - install -m 0755 ${S}/aesdsocket ${D}${bindir}/
#   - install -d ${D}${sysconfdir}/init.d
#   - install -m 0755 ${S}/aesdsocket-start-stop ${D}${sysconfdir}/init.d/S99aesdsocket
```

### Block 3: Update core-image-aesd.bb
```
# Uncomment CORE_IMAGE_EXTRA_INSTALL += "aesd-assignments"
# (openssh already present — keep it)
```

### Block 4: Build
```
# Run ./build.sh which:
#   - Initializes submodules (poky)
#   - Sources poky/oe-init-build-env
#   - Appends MACHINE = "qemuarm64" to local.conf
#   - Adds meta-aesd layer
#   - Runs: bitbake core-image-aesd
```

### Block 5: Verify
```
# Run ./runqemu.sh to boot QEMU
# Verify aesdsocket starts automatically (/etc/init.d/S99aesdsocket runs at boot)
# Run ./full-test.sh against QEMU target IP
```

## Key Decisions
- **HTTPS protocol** for SRC_URI: SSH requires key setup in Yocto environment; HTTPS is simpler
  for a public assignment repo.
- **SRCREV pinned**: Reproducibility — Yocto requires exact SRCREV, not floating HEAD.
- **`-pthread` in TARGET_LDFLAGS**: aesdsocket uses pthreads for thread management; linker
  must be told explicitly on some cross-toolchains.
- **init.d install path**: Using `${sysconfdir}/init.d/` which maps to `/etc/init.d/` in the
  rootfs; the S99 prefix ensures aesdsocket starts last in the init sequence.

## References
- Yocto Kirkstone quick start: https://docs.yoctoproject.org/3.4/brief-yoctoprojectqs/
- oe-init-build-env: sources environment for bitbake
- SRCREV: https://docs.yoctoproject.org/ref-manual/variables.html#term-SRCREV
