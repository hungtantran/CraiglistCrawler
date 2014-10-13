import sys
import re

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser
import MySQLdb

db = MySQLdb.connect(host="pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")

class MLStripper(HTMLParser):
  def __init__(self):
      self.reset()
      self.fed = []
  def handle_data(self, d):
      self.fed.append(d)
  def get_data(self):
      return ''.join(self.fed)

def strip_tags(html):
  s = MLStripper()
  s.feed(html)
  return s.get_data()

def extract_prices(text):
  moneyPatterns = open("moneypatterns.txt", "r").readlines()
  moneyPatterns = [x.strip() for x in moneyPatterns]
  moneyPattern = "|".join(moneyPatterns)
  print moneyPattern
  regex = re.compile(moneyPattern)
  prices = regex.findall(text)
  return prices

def extract_quantities(text, quantitypatterns):
  quantityPatterns = open(quantitypatterns, "r").readlines()
  quantityPatterns = [x.strip() for x in quantityPatterns]
  quantityPattern = "|".join(quantityPatterns)
  regex = re.compile(quantityPattern)
  quantities = regex.findall(text)
  return quantities

def extract_quantities(text):
  quantities_english = extract_quantities(text, "quantitypatterns_english.txt")
  quantities_metric = extract_quantities(text, "quantitypatterns_metrix.txt")

def write_to_db(prices, quantities):
  

  pass

def index(htmlFile):
  # html_doc = open(htmlFile, 'r')
  # soup = BeautifulSoup(html_doc.read())
  html_doc = htmlFile
  soup = BeautifulSoup(html_doc)
  posting = soup.find(id="postingbody")

  text = strip_tags(str(posting))
  print text

  prices = extract_prices(text)
  print prices
  quantities = extract_quantities(text)
  print quantities

def main():
  cursor = db.cursor()
  cursor.execute("SELECT * FROM RawHTML")
  for row in cursor.fetchall():
    htmltext = row[2]
    # print htmltext
    index(htmltext)

  # htmlFile = sys.argv[1]
  # index(htmlFile)

if __name__ == "__main__":
  main()