
# health-indicators

This service provides metrics about the health of a repository on the Platform.

#### How it works
The service collects information from various sources which PlatOps deems important to services health, and stores it in a Mongo repository.

# Nomenclature
- Rater
  - A Rater is the specific metric a repo is scored on e.g. leak-detection, readME.
  - Each Rater uses unique functionally depenendent on the metric
  - These Raters create Ratings
- Rating
  - A Rating is the un-weighted score applied to each repo, for each metric
  - These can be seen as default Ratings
- Weight
  - A weight is a calculation applied to all of a repos Ratings, to produce a *weighted score*
  - A weighted score is what determines the health of a repo/service

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
