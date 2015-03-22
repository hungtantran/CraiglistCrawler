import time
import MySQLdb

db = MySQLdb.connect(host="localhost",
                     user="root",
                     passwd="",
                     db="weedpricelink")

# db = MySQLdb.connect(host="powdb.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
#                      port=4200,user="cedro",
#                      passwd="password",
#                      db="powdb")

def main():
    cursor = db.cursor()
    cursor.execute("SELECT id, url FROM rawhtml WHERE dateCrawled is NULL")
    rows = cursor.fetchall()

    for row in rows:
        id = row[0]
        url = row[1]

        cursor = db.cursor()
        selectQuery = "SELECT date_crawled, time_crawled FROM link_crawled_table WHERE link=\"%s\"" % (url)

        print(selectQuery)

        cursor.execute(selectQuery);
        locations = cursor.fetchall()

        if len(locations) != 1:
            print("ERROR: more or less than 1 key for link crawled")
            continue

        dateCrawled = locations[0][0];
        timeCrawled = locations[0][1];

        # Update rawhtml
        updateQuery = "UPDATE rawhtml SET dateCrawled=\"%s\", timeCrawled=\"%s\" WHERE id = %d" % (dateCrawled, timeCrawled, id)

        print updateQuery

        cursor = db.cursor()
        cursor.execute(updateQuery)
        db.commit()

        # Update posting_location
        updateQuery = "UPDATE posting_location SET datePosted=\"%s\", timePosted=\"%s\" WHERE location_fk = %d" % (dateCrawled, timeCrawled, id)

        print updateQuery

        cursor = db.cursor()
        cursor.execute(updateQuery)
        db.commit()

if __name__ == "__main__":
  main()

