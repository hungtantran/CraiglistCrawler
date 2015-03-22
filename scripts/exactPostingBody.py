import time
import MySQLdb
import string

import sys
sys.setrecursionlimit(10000) 

from bs4 import BeautifulSoup

# db = MySQLdb.connect(host="localhost",
#                      user="root",
#                      passwd="",
#                      db="weedpricelink")

db = MySQLdb.connect(host="powdb.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")

def innerHTML(element):
    return element.decode_contents(formatter="html")

def getPostingBody(html):
    soup = BeautifulSoup(html)

    for anchor in soup.find_all('section'):
        id = anchor.get('id');

        if id == 'postingbody':
            postingbody = innerHTML(anchor).replace("</br>", "")
            return postingbody;

def main():
    cursor = db.cursor()
    cursor.execute("SELECT location_fk FROM posting_location WHERE posting_body IS NULL")
    rows = cursor.fetchall()

    for row in rows:
        id = row[0]

        cursor = db.cursor()
        selectQuery = "SELECT html FROM rawhtml WHERE id=%d" % (id)

        print(selectQuery)

        cursor.execute(selectQuery);
        html = cursor.fetchall()

        if len(html) != 1:
            print("ERROR: more or less than 1 key for html")
            continue

        postingBody = html[0][0];
        postingBody = getPostingBody(postingBody);
        postingBody = MySQLdb.escape_string(postingBody.encode('ascii', 'ignore'));
        postingBody = postingBody.strip(' \t\n\r');

        updateQuery = "UPDATE posting_location SET posting_body=\"%s\" WHERE location_fk = %d" % (postingBody.encode('ascii', 'ignore'), id)

        print updateQuery

        cursor.execute(updateQuery)
        db.commit()


if __name__ == "__main__":
  main()