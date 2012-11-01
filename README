# Submission

This is a minimal web application to demonstrate a simple way to persist HTML form submissions.

Submissions are persisted using [Journal.IO](https://github.com/sbtourist/Journal.IO), which provides thread-safe file-based persistence.

This may turn out to be fast and scalable for a large number of requests, for the case that there are many submissions but that only have to be read once. This is because the application writes the raw request data directly to the journal, which is designed for fast writes, and only parses the form-encoded data when it is read.

## Usage

Submit HTTP POST requests to `/`.

## To do

* Add a way for the request to specify a redirect URL with the request, e.g. in the query string.
* Add ODF spreadsheet export for all submissions.