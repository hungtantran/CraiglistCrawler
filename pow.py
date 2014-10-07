from bs4 import BeautifulSoup
import urllib2
import xml.dom.minidom
from xml.dom.minidom import parse, parseString

response = urllib2.urlopen("http://www.priceofweed.com/data/all")
html = response.read()
soup = BeautifulSoup(html)

outfile = open("/home/ec2-user/pow/pow/prices.txt", "a")

rows = soup.findAll("tr")
for row in rows:
	xmldoc = parseString(str(row).replace("\n", " "))
	toplevel = xmldoc.childNodes[0]

	result = ""
    for td in toplevel.childNodes:
		if td.nodeType != td.TEXT_NODE:
			if (td.childNodes[0].nodeType == td.TEXT_NODE):
				# print td.childNodes[0].nodeValue.strip(),
				result = result + td.childNodes[0].nodeValue.strip() + ","
				if len(td.childNodes) > 1 and len(td.childNodes[1].childNodes) > 0:
					# print td.childNodes[1].childNodes[0].nodeValue.strip()
					result = result[:-1] + td.childNodes[1].childNodes[0].nodeValue.strip() + ","
	result = result[:-1] + "\n"
	outfile.write(result)
	# result = ""
	# contents = row.contents
	# for content in contents:
	# 	if content.name == "td" and content.string != None:
	# 		result = result + (content.string).strip() + ","
	# result = result[:-1] + "\n"
	# outfile.write(result)
	# 
