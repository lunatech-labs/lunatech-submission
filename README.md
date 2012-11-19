# Submission

## Purpose

‘Submission’ is a minimal web application to demonstrate a simple way to persist HTML form submissions.

The use case is short-lived marketing campaigns where thousands of people submit an HTML form during a few days or weeks, after which the submissions are collected. This calls for a simple solution that’s easy to run.

## Usage

Submit HTML forms with method POST to `/`. This is an HTTP POST request, for example (omitting various headers):

    POST / HTTP/1.1
    Content-Type: application/x-www-form-urlencoded

    age=18-30&sex=male&location=Rotterdam

To redirect to another page after the submission, add a URL in the request’s `redirect` query string parameter, e.g. `POST /?redirect=http%3A%2F%2Fexample.com%2Fthank-you`.

## Technical design

Submissions are persisted using [Journal.IO](https://github.com/sbtourist/Journal.IO), which provides thread-safe file-based persistence.

This may turn out to be fast and scalable for a large number of requests, for the case that there are many submissions but that only have to be read once. This is because the application writes the raw request data directly to the journal, which is designed for fast writes, and only parses the form-encoded data when it is read.

This version does not use Akka, but it would presumably be possible to introduce an Actor front-end to the event store to allow massive scalability.

## To do

* Secure access to the export.
* Add ODF spreadsheet export for all submissions.

## Benchmarks

To get a rough idea of how this works with Play, use Apache Bench:

`ab =k -n 10000 -c 1 -p public/example.txt -T "application/x-www-form-urlencoded" "http://localhost:9000/?redirect=http%3A%2F%2Fexample.com%2Fthank-you"`

Results from a MacBook Pro, 2.7 GHz Intel Core i7, 8 GB, SSD:

* c=1, requests/s=3160
* c=2, requests/s=4690
* c=5, requests/s=8130
* c=10, requests/s=9980
* c=20, requests/s=11700
* c=50, requests/s=12900
* c=100, requests/s=13400

