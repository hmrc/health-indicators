
# health-indicators

This service provides metrics about the health of a repository on the Platform.

#### How it works
- Collects information from various sources which are deemed important to a service's health, and stores them in a Mongo repository
- A total score will be calculated that represents the health of a service

# Nomenclature
- Rater
  - The evaluation of a specific metric for a github repo e.g. leak-detection, readME.
  - Each Rater uses unique functionally dependent on the metric
  - Raters create an Indicator
  
- Repository Health Indicator
   - Each repo has one Repository Health Indicator which is stored in Mongo
   - An Indicator contains the indicator type, results of each metric
   - Results contain a result type and description
   - One Indicator can have multiple results

- Score
   - The number of "points" given to a repo based on the results inside Indicators
   - Can be positive or negative depending on the results
   - The total number of points defines a Rating Score for a repo

- Rating
  - Displays the Service Health of a repo that includes:
    - Rating Score (total score)
    - Ratings for each repo:
        - Rating Type e.g leak-detection, readME
        - Score given for each rating based on the Indicator
        - Breakdown of the Score with a description

### How to Run Locally
- Start catalogue with Service Manager: sm --start CATALOGUE
- Run with sbt and github access token: sbt -Dgithub.open.api.token=<> run

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
