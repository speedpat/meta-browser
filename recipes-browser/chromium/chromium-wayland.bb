DESCRIPTION = "Chromium browser"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://src/LICENSE;md5=537e0b52077bf0a616d0a0c8a79bc9d5"
DEPENDS = "xz-native pciutils pulseaudio cairo nss zlib-native libav cups ninja-native gconf depot-tools-native"
DEPENDS[x11] = "libxi xextproto gtk+ libxss"
EXTRANATIVEPATH= "depot_tools"
DEPENDS := "${@oe_filter_out('^(libgnome-keyring)$', '${DEPENDS}', d)} libfslvpuwrap"
RDEPENDS_${PN} = "libgal-mx6 libegl-mx6 libgles2-mx6 ttf-liberation-mono ttf-liberation-sans ttf-liberation-serif ttf-dejavu-sans ttf-dejavu-sans-mono ttf-dejavu-sans-condensed ttf-dejavu-serif ttf-dejavu-serif-condensed"
INSANE_SKIP_${PN} = "installed-vs-shipped"
INHIBIT_PACKAGE_STRIP = "0"
PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"
SRC_URI = "\
        file://include.gypi \
        file://oe-defaults.gypi \
        file://unistd-2.patch;patchdir=src \
        file://google-chrome \
        file://google-chrome.desktop \
        file://multitouch.gypi \
        file://0001-browser-Support-Desktop-Aura-creation-on-Ozone.patch;patchdir=src \
        file://0002-disable-cups.patch;patchdir=src \
        file://0003-enable-touch-aura.patch;patchdir=src \
	file://0001-Hide-X11-dependencies-when-use_x11_0.patch;patchdir=src \
        file://0001-remove-gtk-from-BUILD.gn.patch;patchdir=src \
        file://0102-commit-patches.patch;patchdir=src \
        file://0002-EGL-specific-changes-for-Wayland.patch;patchdir=src \
        file://1001-Mesa-fix.patch;patchdir=src/third_party/mesa/src \
        file://0004-fix-link-flags.patch;patchdir=src \
        file://0001-accept-wayland-version-1.0.0-as-wayland-egl.pc-state.patch;patchdir=src/ozone \
"

PR = "r2"

# include.gypi exists only for armv6 and armv7a and there isn't something like COMPATIBLE_ARCH afaik
COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_i586 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"

inherit gettext

FETCH_DEPENDENCY = "depot-tools-native"
EXTRANATIVEPATH= "depot_tools"

DEPS_FILE = "https://github.com/speedpat/ozone-wayland.git"
DEPS_REV = "316cfa7bd7e705a1aa73a0ed612ba1ed2d087815"

python do_fetch_gclient() {
    import os
    import bb
    from   bb    import data
    from   bb.fetch2 import FetchMethod
    from   bb.fetch2 import runfetchcmd    
    from   bb.fetch2 import logger

    src_uri = d.getVar('DEPS_FILE', True)
    src_rev = d.getVar('DEPS_REV', True)

    runfetchcmd("gclient config --name=src/ozone --git-deps %s " % src_uri, d)
    runfetchcmd("GYP_CHROMIUM_NO_ACTION=1 GYP_DEFINES=\"use_ash=0 use_aura=1 chromeos=0 use_ozone=1\" gclient sync --reset --nohooks --revision %s" % src_rev, d)
}
python do_fetch() {
    try:
        bb.build.exec_func('base_do_unpack', d)
    except:
        raise

    bb.build.exec_func('do_fetch_gclient', d)
}

python do_patch() {
     os.chdir('${S}/src')
     bb.build.exec_func('patch_do_patch', d)
}

do_patch[dirs] = "${S}/src"

EXTRA_OEGYP =	" \
	${@base_contains('DISTRO_FEATURES', 'ld-is-gold', '', '-Dlinux_use_gold_binary=0', d)} \
	${@base_contains('DISTRO_FEATURES', 'ld-is-gold', '', '-Dlinux_use_gold_flags=0', d)} \
	-I ${WORKDIR}/oe-defaults.gypi \
	-I ${WORKDIR}/include.gypi \
    -I ${WORKDIR}/multitouch.gypi \
    -Dcpu_arch=arm \
    -Darm_float_abi=hard \
    -Duse_x11=0 \
	-f ninja \
"
ARMFPABI_armv7a = "${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'arm_float_abi=hard', 'arm_float_abi=softfp', d)}"

export GYP_DEFINES="${ARMFPABI_armv7a} release_extra_cflags='-Wno-error=unused-local-typedefs' sysroot=''"
do_configure() {

    cd ${S}/src
    GYP_CHROMIUM_NO_ACTION=1 gclient runhooks
    mkdir -p ${S}/src/oe-defaults
    cp ${WORKDIR}/oe-defaults.gypi ${S}/src/oe-defaults/supplement.gypi
    mkdir -p ${S}/src/oe-include
    cp ${WORKDIR}/include.gypi ${S}/src/oe-include/supplement.gypi
    mkdir -p ${S}/src/oe-multitouch
    cp ${WORKDIR}/multitouch.gypi ${S}/src/oe-multitouch/supplement.gypi
    # replace LD with CXX, to workaround a possible gyp issue?
    LD="${CXX}" export LD
    CC="${CC}" export CC
    CXX="${CXX}" export CXX
    CC_host="gcc" export CC_host
    CXX_host="g++" export CXX_host
    build/gyp_chromium --depth=. ${EXTRA_OEGYP}
}

do_compile() {

	# build with ninja
	ninja -vC ${S}/src/out/Release chrome

	# build unittests
 #	if [ "${CHROME_UNIT_TESTS}" = "yes" ]; then
        # browser_tests removed until fix
#	ninja -C ${S}/out/Debug base_unittests \
#		cacheinvalidation_unittests \
#		chromedriver2_unittests \
#		crypto_unittests \
#		gpu_unittests \
#		interactive_ui_tests \
#		ipc_tests \
#		jingle_unittests \
#		media_unittests \
#		net_perftests \
#		sql_unittests \
#		sync_integration_tests \
#		sync_unit_tests \
#		views_unittests \
#		webkit_unit_tests \
#		performance_ui_tests \
#		ui_unittests \
#		aura_unittests \
#		breakpad_unittests \
#		cc_unittests \
#		components_unittests \
#		compositor_unittests \
#		crypto_unittests \
#		dbus_unittests \
#		device_unittests \
#		google_apis_unittests \
#		message_center_unittests \
#		sandbox_linux_unittests \
#		url_unittests \
#		webkit_compositor_bindings_unittests \
#		wtf_unittests
#	fi
}

do_install() {
	install -d ${D}/usr/chrome
	install -m 0755 ${WORKDIR}/google-chrome ${D}/usr/chrome

	install -d ${D}${datadir}/applications
	install -m 0644 ${WORKDIR}/google-chrome.desktop ${D}${datadir}/applications/

	install -d ${D}/usr/chrome/
	install -m 0755 ${S}/src/out/Release/chrome ${D}/usr/chrome/chrome
#	install -m 0644 ${S}/src/out/Release/chrome.pak ${D}/usr/chrome/
	install -m 0644 ${S}/src/out/Release/resources.pak ${D}/usr/chrome/
	install -m 0644 ${S}/src/out/Release/chrome_100_percent.pak ${D}/usr/chrome/
	install -m 0644 ${S}/src/out/Release/product_logo_48.png ${D}/usr/chrome/
	install -m 0644 ${S}/src/out/Release/icudtl.dat ${D}/usr/chrome/
	install -m 0755 ${S}/src/out/Release/libffmpegsumo.so ${D}/usr/chrome/
	install -d ${D}${libdir}/chrome/
	install -m 0755 ${S}/src/out/Release/lib/*.so ${D}${libdir}/chrome/

	install -d ${D}/usr/chrome/locales/
	install -m 0644 ${S}/src/out/Release/locales/en-US.pak ${D}/usr/chrome/locales

	# install unittest

#	if [ "${CHROME_UNIT_TESTS}" = "yes" ]; then
#		install -d ${D}/usr/chrome
#		install -m 0755 ${S}/out/Debug/*tests ${D}/usr/chrome/
#		install -m 0644 ${S}/out/Debug/*.pak ${D}/usr/chrome/
#		install -m 0755 ${S}/out/Debug/*.so ${D}/usr/chrome/

#		cp ${S}/../unittest/run_unittests.py ${D}/usr/chrome/
#		chmod 755 ${D}/usr/chrome/run_unittests.py

#		install -d ${D}/usr/chrome/locales
#		install -m 0644 ${S}/out/Debug/locales/* ${D}/usr/chrome/locales

#		install -d ${D}${libdir}/chrome-unittest/
#		install -m 0755 ${S}/out/Debug/lib/*.so ${D}${libdir}/chrome-unittest/

#		install -d ${D}/usr/chrome/test/data
#		cp -r -p ${S}/chrome/test/data/* ${D}/usr/chrome/test/data/

#		install -d ${D}/usr/content/test/data
#		cp -r -p ${S}/content/test/data/* ${D}/usr/content/test/data/

#		install -d ${D}/usr/media/test/data
#		cp -r -p ${S}/media/test/data/* ${D}/usr/media/test/data

#		install -d ${D}/usr/net/test/data
#		cp -r -p ${S}/net/data/* ${D}/usr/net/test/data

#		install -d ${D}/usr/components/test/data
#		cp -r -p ${S}/components/test/data/* ${D}/usr/components/test/data

#		install -d ${D}/usr/third_party/WebKit/LayoutTests
#		cp -r -p ${S}/third_party/WebKit/LayoutTests/* ${D}/usr/third_party/WebKit/LayoutTests

#		install -d ${D}/usr/third_party/WebKit/PerformanceTests
#		cp -r -p ${S}/third_party/WebKit/PerformanceTests/* ${D}/usr/third_party/WebKit/PerformanceTests

#		install -d ${D}/usr/third_party/WebKit/ManualTests
#		cp -r -p ${S}/third_party/WebKit/ManualTests/* ${D}/usr/third_party/WebKit/ManualTests

#		install -d ${D}/usr/third_party/WebKit/Source/web/tests/data
#		cp -r -p ${S}/third_party/WebKit/Source/web/tests/data/* ${D}/usr/third_party/WebKit/Source/web/tests/data

#		install -d ${D}/usr/third_party/WebKit/LayoutTests/fast
#		cp -r -p ${S}/../fast/* ${D}/usr/third_party/WebKit/LayoutTests/fast

#		install -d ${D}/usr/net/tools/testserver/
#		install -m 0755 ${S}/net/tools/testserver/*.py ${D}/usr/net/tools/testserver

#		install -d ${D}/usr/sync/tools/testserver/
#		install -m 0755 ${S}/sync/tools/testserver/*.py ${D}/usr/sync/tools/testserver/

#		install -d ${D}/usr/device/test/data
#		cp -r -p ${S}/device/test/data/* ${D}/usr/device/test/data

#		install -d ${D}/usr/base/test/data
#		cp -r -p ${S}/base/test/data/* ${D}/usr/base/test/data

#		install -d ${D}/usr/cc/test/data
#		cp -r -p ${S}/cc/test/data/* ${D}/usr/cc/test/data

#		install -d ${D}/usr/printing/test/data
#		cp -r -p ${S}/printing/test/data/* ${D}/usr/printing/test/data

#		install -d ${D}/usr/chrome_frame/test/data
#		cp -r -p ${S}/chrome_frame/test/data/* ${D}/usr/chrome_frame/test/data

#		install -d ${D}/usr/ui/gfx/test/data
#		cp -r -p ${S}/ui/gfx/test/data/* ${D}/usr/ui/gfx/test/data

#		install -d ${D}/usr/ui/base/test/data
#		cp -r -p ${S}/ui/base/test/data/* ${D}/usr/ui/base/test/data

#		install -d ${D}/usr/extensions/test/data
#		cp -r -p ${S}/extensions/test/data/* ${D}/usr/extensions/test/data

#		install -d ${D}/usr/chromeos/test/data
#		cp -r -p ${S}/chromeos/test/data/* ${D}/usr/chromeos/test/data

#		install -d ${D}/usr/third_party/zlib/google/test/data
#		cp -r -p ${S}/third_party/zlib/google/test/data/* ${D}/usr/third_party/zlib/google/test/data

#		install -d ${D}/usr/third_party/pywebsocket/src/mod_pywebsocket
#		cp -r -p ${S}/third_party/pywebsocket/src/mod_pywebsocket/* ${D}/usr/third_party/pywebsocket/src/mod_pywebsocket

#		# test data for net_perftests
#		install -d ${D}/usr/net/data
#		cp -r -p ${S}/net/data/* ${D}/usr/net/data

#		# sync tests python dependencies
#		cp -r -p ${S}/out/Debug/pyproto/* ${D}/usr
#		cp -r -p ${S}/out/Debug/pyproto/sync/protocol/* ${D}/usr/sync/tools/testserver

#		# remove binary blobs
#		rm ${D}/usr/chrome/test/data/extensions/uitest/plugins_private/plugin32.so
#		rm ${D}/usr/chrome/test/data/extensions/uitest/plugins_private/plugin64.so
#		rm ${D}/usr/chrome/test/data/extensions/uitest/plugins/plugin32.so
#		rm ${D}/usr/chrome/test/data/extensions/uitest/plugins/plugin64.so
#		rm ${D}/usr/chrome/test/data/components/ihfokbkgjpifnbbojhneepfflplebdkc/ihfokbkgjpifnbbojhneepfflplebdkc_2/a_changing_binary_file
#		rm ${D}/usr/chrome/test/data/components/ihfokbkgjpifnbbojhneepfflplebdkc/ihfokbkgjpifnbbojhneepfflplebdkc_1/a_changing_binary_file
#	fi
}

FILES_${PN} = "/usr/chrome/ /usr/google-chrome ${datadir}/applications ${libdir}/chrome/"
FILES_${PN}-dbg = "/usr/chrome/.debug/ ${libdir}/chrome/.debug"

FILES_${PN} += "${@base_conditional("CHROME_UNIT_TESTS", "yes", "${libdir}/chrome-unittest/ /usr/net /usr/chrome /usr/third_party /usr/content /usr/media /usr/components /usr/sync /usr/device /usr/base /usr/cc /usr/printing /usr/chrome_frame /usr/ui /usr/extensions /usr/chromeos /usr/components /usr/dbus /usr/google /usr/gpu /usr/policy /usr/remoting /usr/webrtc", "", d)}"
FILES_${PN}-dbg += "${@base_conditional("CHROME_UNIT_TESTS", "yes","/usr/chrome/.debug ${libdir}/chrome-unittest/.debug", "", d)}"
