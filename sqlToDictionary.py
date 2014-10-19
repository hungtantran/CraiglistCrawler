import os
import re


import MySQLdb

db = MySQLdb.connect(host="pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")

def main():
  words = []

  cursor = db.cursor()
  cursor.execute("SELECT * from RawHTML")
  for row in cursor.fetchall():
    htmlName = row[1].rsplit('/',1)[1]
    htmlText = row[2]

    htmlWords = re.split('; |, |\*|<>\n', htmlText)
    for word in htmlWords:
      if len(word) > 30:
        continue
      word = word.strip()
      word = re.sub(r'\W+', '', word)
      words.append(word)

  wordfile = open("./dictionary.txt", 'w')
  words = set(words)
  for word in words:
    wordfile.write(word + "\n")


if __name__ == "__main__":
  main()