from bs4 import BeautifulSoup
import urllib2

response = urllib2.urlopen("http://www.priceofweed.com/data/all")
html = response.read()
soup = BeautifulSoup(html)

outfile = open("/home/ec2-user/pow/pow/prices.txt", "a")

rows = soup.findAll("tr")
for row in rows:
	result = ""
	contents = row.contents
	for content in contents:
		if content.name == "td" and content.string != None:
			result = result + (content.string).strip() + ","
	result = result[:-1] + "\n"
	outfile.write(result)
	
