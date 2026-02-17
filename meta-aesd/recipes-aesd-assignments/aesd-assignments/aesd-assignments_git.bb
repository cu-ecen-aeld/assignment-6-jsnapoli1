# See https://git.yoctoproject.org/poky/tree/meta/files/common-licenses
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Pseudocode: Set SRC_URI to the public assignments-3-and-later repo using https protocol.
# Using https avoids the need for SSH key configuration in the Yocto build environment.
# The branch is master where Assignment 6 Part 1 was implemented.
SRC_URI = "git://github.com/cu-ecen-aeld/assignments-3-and-later-jsnapoli1.git;protocol=https;branch=master"

PV = "1.0+git${SRCPV}"
# Pseudocode: Pin to the exact commit of the Part 1 implementation for reproducibility.
# Yocto requires a fixed SRCREV — floating HEAD ("AUTOINC") is not allowed for production.
SRCREV = "faa59dd95473a46b4901f6ea56adac884509bbcb"

# S points to the "server" directory inside the fetched repo, where aesdsocket.c lives.
S = "${WORKDIR}/git/server"

# Pseudocode: Tell the linker to include pthreads library.
# aesdsocket uses pthread_create/join/mutex — the cross-toolchain needs this flag explicitly.
TARGET_LDFLAGS += "-pthread -lrt"

# Pseudocode: Declare all files installed into the rootfs so Yocto can package them.
# ${bindir} = /usr/bin, ${sysconfdir} = /etc
FILES:${PN} += "${bindir}/aesdsocket"
FILES:${PN} += "${sysconfdir}/init.d/S99aesdsocket"

do_configure () {
	# No autoconf/cmake — Makefile-based project, nothing to configure.
	:
}

do_compile () {
	# Pseudocode: Run the project Makefile using Yocto's cross-compile wrapper.
	# oe_runmake sets CC, CFLAGS, LDFLAGS from the Yocto toolchain automatically.
	oe_runmake
}

do_install () {
	# Pseudocode: Install aesdsocket binary to /usr/bin in the rootfs staging area.
	# ${D} is the destination directory (staging rootfs), ${bindir} resolves to /usr/bin.
	install -d ${D}${bindir}
	install -m 0755 ${S}/aesdsocket ${D}${bindir}/aesdsocket

	# Pseudocode: Install the init script to /etc/init.d so it runs at boot.
	# The S99 prefix ensures this starts after most other init services (higher number = later).
	# start-stop-daemon -d flag starts the daemon in background mode.
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/aesdsocket-start-stop ${D}${sysconfdir}/init.d/S99aesdsocket
}

# Pseudocode: Register the init script with the update-rc.d class for proper symlink creation.
# This causes Yocto to call update-rc.d which creates /etc/rc*.d/ symlinks at image creation time.
inherit update-rc.d
INITSCRIPT_NAME = "S99aesdsocket"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 20 0 1 6 ."
