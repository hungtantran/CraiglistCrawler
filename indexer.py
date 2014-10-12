import sys
import re

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser

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
  moneyPatterns = open("moneyPatterns.txt", "r").readlines()
  moneyPatterns = [x.strip() for x in moneyPatterns]
  moneyPattern = "|".join(moneyPatterns)
  print moneyPattern
  regex = re.compile(moneyPattern)
  prices = regex.findall(text)
  return prices

def extract_quantities(text):
  quantityPatterns = ["\d+/\d+", "eighth", "eighths", "quarter", "quart", "quarters", "half oz", "ounce", "oz", "\d+[-+]?[0-9]*\.?[0-9]+ grams"]
  quantityPattern = "|".join(quantityPatterns)
  regex = re.compile(quantityPattern)
  quantities = regex.findall(text)
  return quantities

def index(htmlFile):
  html_doc = open(htmlFile, 'r')
  soup = BeautifulSoup(html_doc.read())
  posting = soup.find_all("body")[0]
  text = strip_tags(str(posting))
  print text

  prices = extract_prices(text)
  print prices
  quantities = extract_quantities(text)
  print quantities

def main():
  htmlFile = sys.argv[1]
  index(htmlFile)

if __name__ == "__main__":
  main()