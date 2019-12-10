#!/bin/bash


echo ""
echo "---------------------------------------------------------------------------------------------------------"
echo "Show screenings on 2019-12-15 that starts beetween 9AM and 8PM sorted by movie titles and screening times"
echo "---------------------------------------------------------------------------------------------------------"
echo ""
echo ""

FROM=2019-12-15T09:00Z
TO=2019-12-15T20:00Z

curl localhost:8080/screenings?from=$FROM\&to=$TO | python -m json.tool

echo ""
echo "---------------------------------------------------------------------------------------------------------"
echo "Select Gladiator at 10AM (screeningId = 11)"
echo "---------------------------------------------------------------------------------------------------------"
echo ""

curl localhost:8080/screening/11 | python -m json.tool

echo ""
echo "---------------------------------------------------------------------------------------------------------"
echo "Now Mr Nowak make reservation for his whole family"
echo "He take:"
echo " * place 2 and 3 in the first row for him and his wife"
echo " * place 4 in the second row for his oldest son"
echo " * place 4, 5, 6 in the third row for kids"
echo ""
echo "Mr Nowa should paid 2 * 25 + 1 * 18 + 3 * 12,50 = 105,50"
echo "Reservation expires at $(date -d "+1 day") or just before screenign"
echo "---------------------------------------------------------------------------------------------------------"
echo ""

RESERVATION_JSON='{
	"screeningId": 11,
	"bookingUser": {
		"name": "Wojciech",
		"surname": "Nowak"
	},
	"seatsToReserve": [
	 	{"row": 1, "column": 2, "reservationType":"ADULT"},
	 	{"row": 1, "column": 3, "reservationType":"ADULT"},
	 	{"row": 2, "column": 4, "reservationType":"STUDENT"},
	 	{"row": 3, "column": 4, "reservationType":"CHILD"},
	 	{"row": 3, "column": 5, "reservationType":"CHILD"},
	 	{"row": 3, "column": 6, "reservationType":"CHILD"}
	]
}'
echo $RESERVATION_JSON
echo $RESERVATION_JSON | curl -X POST localhost:8080/reserve -H "Content-Type: application/json" -d @- | python -m json.tool