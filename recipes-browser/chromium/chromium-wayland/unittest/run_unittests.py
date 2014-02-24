#!/usr/bin/python

import errno
import os
import subprocess
import sys

class CommandNotFound(Exception): pass

# set path for unittest libraries
os.putenv('LD_LIBRARY_PATH', '/usr/lib/chrome-unittest/')
# set X11 display
os.putenv('DISPLAY', ':0.0')
# set CR ROOT
os.putenv('CR_SOURCE_ROOT', '/usr')
# set PYTHONPATH to chrome specific stuff
os.putenv('PYTHONPATH', '/usr')

# all available unittests and tests
# browser_tests isn't added to run as they took ~8 hours to run (3000 tests and ~10s to run one test)
COMMANDS = ["base_unittests", "cacheinvalidation_unittests", "chromedriver2_unittests",
            "crypto_unittests", "gpu_unittests", "gpu_tests", "interactive_ui_tests", "ipc_tests",
            "jingle_unittests", "media_unittests", "net_perftests", "sql_unittests", "sync_integration_tests",
            "sync_unit_tests", "views_unittests", "webkit_unit_tests", "performance_ui_tests", "ui_unittests",
            "aura_unittests", "breakpad_unittests", "cc_unittests", "components_unittests", "compositor_unittests",
            "crypto_unittests", "dbus_unittests", "device_unittests", "google_apis_unittests", "message_center_unittests",
            "sandbox_linux_unittests", "url_unittests", "webkit_compositor_bindings_unittests", "weborigin_unittests",
            "wtf_unittests"]

# function overtaken from google process_utils.py
def RunCommandFull(command, verbose=True, collect_output=False,
                   print_output=True):
  """Runs the command list.

  Prints the given command (which should be a list of one or more strings).
  If specified, prints its stderr (and optionally stdout) to stdout,
  line-buffered, converting line endings to CRLF (see note below).  If
  specified, collects the output as a list of lines and returns it.  Waits
  for the command to terminate and returns its status.

  Args:
    command: the full command to run, as a list of one or more strings
    verbose: if True, combines all output (stdout and stderr) into stdout.
             Otherwise, prints only the command's stderr to stdout.
    collect_output: if True, collects the output of the command as a list of
                    lines and returns it
    print_output: if True, prints the output of the command

  Returns:
    A tuple consisting of the process's exit status and output.  If
    collect_output is False, the output will be [].

  Raises:
    CommandNotFound if the command executable could not be found.
  """
  print '\n' + subprocess.list2cmdline(command).replace('\\', '/') + '\n', ###

  if verbose:
    out = subprocess.PIPE
    err = subprocess.STDOUT
  else:
    out = file(os.devnull, 'w')
    err = subprocess.PIPE
  try:
    full_command = []                                              
    full_command.append(command)                                         
    full_command.append('--disable-setuid-sandbox')
    full_command.append('--locale_pak=/usr/chrome/locales/en-US.pak')
    proc = subprocess.Popen(full_command, stdout=out, stderr=err, bufsize=1)
  except OSError, e:
    if e.errno == errno.ENOENT:
      raise CommandNotFound('Unable to find "%s"' % command)
    raise

  output = []

  if verbose:
    read_from = proc.stdout
  else:
    read_from = proc.stderr
  line = read_from.readline()
  while line:
    line = line.rstrip()

    if collect_output:
      output.append(line)

    if print_output:
      # Windows Python converts",n to",r\n automatically whenever it
      # encounters it written to a text file (including stdout).  The only
      # way around it is to write to a binary file, which isn't feasible for
      # stdout. So we end up with",r\n here even though we explicitly write
      #",n.  (We could write",r instead, which doesn't get converted to",r\n,
      # but that's probably more troublesome for people trying to read the
      # files.)
      print line + '\n',

      # Python on windows writes the buffer only when it reaches 4k. This is
      # not fast enough for all purposes.
      sys.stdout.flush()
    line = read_from.readline()

  # Make sure the process terminates.
  proc.wait()

  if not verbose:
    out.close()
  return (proc.returncode, output)

# main function
if '__main__' == __name__:
	test_path = "/usr/chrome"
	result_path = "/home/root"
	filename = os.path.join(result_path, "unittests_result.log")
	
	# remove log if exists
	if (os.path.exists(filename)):
		os.remove(filename)

	logfile = open(filename, "w")
	# run all tests
	for cmd in COMMANDS:
		print "Running " + cmd
		command = os.path.join(test_path, cmd)
		print command
		out = RunCommandFull(command, True, True)

		logfile.write("Test: %s - %s\n" %(cmd, "PASSED" if out[0] == 0 else "FAILED"))
		logfile.write("Exit status: %d\n" %(out[0])) 
		logfile.write('\n'.join(out[1]))
		logfile.write("\n\n")
		
	logfile.close();

		

