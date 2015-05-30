import subprocess

ps = subprocess.Popen(['/bin/ps' ,'aux'], stdout=subprocess.PIPE).communicate()[0]
processes = ps.split('\n')

nfields = len(processes[0].split()) - 1

running = False
for row in processes[1:]:
  command = row.split(None, nfields)
  if len(command) <= 0:
    break
  if "app.js" in command[len(command)-1]:
    running = True
    break

if not running:
  subprocess.Popen(["/usr/bin/nohup", "/home/ec2-user/node-v0.12.0-linux-x64/bin/node", "/home/ec2-user/pow/pow/powwebsite/app.js"])


