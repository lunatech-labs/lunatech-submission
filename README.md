# Submission

## Purpose

‘Submission’ is a minimal web application to demonstrate a simple way to persist HTML form submissions.

The use case is short-lived marketing campaigns where thousands of people submit an HTML form during a few days or weeks, after which the submissions are collected. This calls for a simple solution that’s easy to run.

## Usage

Submit HTTP POST requests to `/`.

To redirect to another page after the submission, add a URL in the request’s `redirect` query string parameter, e.g. `POST /?redirect=http%3A%2F%2Fexample.com%2Fthank-you`.

## Technical design

Submissions are persisted using [Journal.IO](https://github.com/sbtourist/Journal.IO), which provides thread-safe file-based persistence.

This may turn out to be fast and scalable for a large number of requests, for the case that there are many submissions but that only have to be read once. This is because the application writes the raw request data directly to the journal, which is designed for fast writes, and only parses the form-encoded data when it is read.

This version does not use Akka, but it would presumably be possible to introduce an Actor front-end to the event store to allow massive scalability.

## To do

* Secure access to the export.
* Add ODF spreadsheet export for all submissions.
