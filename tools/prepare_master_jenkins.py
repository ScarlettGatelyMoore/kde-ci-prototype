'''
Documentation, License etc.

@package prepare_master_jenkins
'''
import json
import urllib3
from pprint import pprint

data_file = json.loads(open('master_plugins.json').read())   

for x in data_file:
  all_plugins = (x['plugins'])
  for plugin in all_plugins:
    data = '<jenkins><install plugin="' + plugin + '@latest" /></jenkins>'
    url = 'http://localhost:8080/pluginManager/installNecessaryPlugins'
    req = urllib3.Request(url, data, {'Content-Type': 'text/xml'})
    f = urllib3.urlopen(req)
    for y in f:
      pprint("Now installing " + plugin)
    f.close()