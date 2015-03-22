import time
import MySQLdb

# db = MySQLdb.connect(host="localhost",
#                      user="root",
#                      passwd="",
#                      db="weedpricelink")

db = MySQLdb.connect(host="powdb.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")

def main():
    cursor = db.cursor()
    cursor.execute("SELECT state, city, location_fk AS id FROM posting_location WHERE location_link_fk IS NULL")
    rows = cursor.fetchall()

    for row in rows:
        state = row[0]
        city = row[1]
        id = row[2]

        cursor = db.cursor()
        selectQuery = "SELECT * FROM location_link WHERE state=\"%s\" AND city=\"%s\";" % (state, city)

        print(selectQuery)

        cursor.execute(selectQuery);
        locations = cursor.fetchall()

        if len(locations) > 1:
            print("ERROR: more than one key for locations")
            continue

        updateQuery = "UPDATE posting_location SET location_link_fk = %d WHERE location_fk = %d" % (locations[0][0], id)

        print updateQuery

        cursor = db.cursor()
        cursor.execute(updateQuery)
        db.commit()

if __name__ == "__main__":
  main()

