import json
import time
import urllib2
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
    cursor.execute("SELECT * FROM location_link WHERE latitude IS NULL")
    rows = cursor.fetchall()

    for row in rows:
        country = row[2]
        state = row[3]
        city = row[4]

        url = "http://maps.google.com/maps/api/geocode/json?address="
        url = (url + city + "+" + state + "+" + country).replace(' ', '+').replace('-', '+')
        print(url)

        response = urllib2.urlopen(url)
        data = json.load(response)

        try:
            location = data['results'][0]['geometry']['location']
            print [location['lat'], location['lng']]

            nelocation = data['results'][0]['geometry']['bounds']['northeast']
            print [nelocation['lat'], nelocation['lng']]

            swlocation = data['results'][0]['geometry']['bounds']['southwest']
            print [swlocation['lat'], swlocation['lng']]

            updateQuery = "UPDATE location_link SET latitude=\"%s\", longitude=\"%s\", nelatitude=\"%s\", nelongitude=\"%s\", swlatitude=\"%s\", swlongitude=\"%s\" WHERE id=%s;" % (str(location['lat']), str(location['lng']), str(nelocation['lat']), str(nelocation['lng']), str(swlocation['lat']), str(swlocation['lng']), str(row[0]))

            print updateQuery

            cursor = db.cursor()
            cursor.execute(updateQuery)
            db.commit()
        except:
            continue;

        time.sleep(1)

if __name__ == "__main__":
  main()
