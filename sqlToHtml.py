import os

import MySQLdb

db = MySQLdb.connect(host="pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")
outdir = "./crawled_files"

def main():
  words = []

  cursor = db.cursor()
  cursor.execute("SELECT * from RawHTML")
  for row in cursor.fetchall():
    htmlName = row[1].rsplit('/',1)[1]
    htmlText = row[2]
    
    htmlFilePath = os.path.join(outdir, htmlName)

    outfile = open(htmlFilePath, 'w')
    outfile.write(htmlText)

    htmlWords = htmlText.split(' ')
    for word in htmlWords:
      words.append(word)

  wordfile = open("./dictionary.txt", 'r')
  for word in words:
    wordfile.write(word + "\n")


if __name__ == "__main__":
  main()