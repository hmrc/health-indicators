
# health-indicators

This service provides metrics about the health of a repository on the Platform.

#### How it works
- Collects information from various sources which are deemed important to a service's health, and stores them in a Mongo repository
- A total score will be calculated that represents the health of a service

# Nomenclature
- Rater
  - The evaluation of a specific metric for a github repo e.g. leak-detection, readME.
  - Each Rater uses unique functionally dependent on the metric
  - Raters create an Indicator to be stored in Mongo
  
- Repository Health Indicator
   - Holds the results of each Rater
   - Each repo has one Repository Health Indicator
   - Contains Indicators for each Rater
   - An Indicator contains the indicator type, results of each metric
   - Results contain a result type and description
   - One Indicator can have multiple results
   
  ```{
         "_id" : ObjectId("60210fdf254e2678df761f21"),
         "repositoryName" : "example-service",
         "timestamp" : ISODate("2021-02-08T13:35:39.274Z"),
         "indicators" : [ 
             {
                 "indicatorType" : "bobby-rule-indicator",
                 "results" : []
             }, 
             {
                 "indicatorType" : "leak-detection-indicator",
                 "results" : []
             }, 
             {
                 "indicatorType" : "read-me-indicator",
                 "results" : [ 
                     {
                         "resultType" : "default-readme",
                         "description" : "Default readme"
                     }
                 ]
             }
         ]
     }```
   
- Score
   - The number of "points" given to a repo based on the results inside Indicators
   - Can be positive or negative depending on the results
   - The total number of points defines a Rating Score for a repo

- Rating
  - Based on the data inside Mongo (Indicators)
  - Displays the Service Health of a repo that includes:
    - Rating Score (total score)
    - Ratings for each repo:
        - Rating Type e.g leak-detection, readME
        - Score given for each rating based on the Indicator
        - Breakdown of the Score with a description

```
   "repositoryName":"example-service",
   "repositoryScore":-50,
   "ratings":[
      {
         "ratingType":"BobbyRule",
         "ratingScore":-100,
         "breakdown":[
            {
               "points":-100,
               "description":"microservice-bootstrap - Critical security upgrade: [CVE](https://confluence.tools.tax.service.gov.uk)"
            }
         ]
      },
      {
         "ratingType":"LeakDetection",
         "ratingScore":0,
         "breakdown":[]
      },
      {
         "ratingType":"ReadMe",
         "ratingScore":50,
         "breakdown":[
            {
               "points":50,
               "description":"Valid readme"
            }
         ]
      }
}]
```


### How to Run Locally
- Start catalogue with Service Manager: sm --start CATALOGUE
- Remember to: sm --stop HEALTH_INDICATORS
- Run with sbt and github access token: sbt -Dgithub.open.api.token=<> run
- Sort JSON by Score: Ascending or Descending order using query params
    - http://localhost:9018/health-indicators/repositories?sort=asc
    - http://localhost:9018/health-indicators/repositories?sort=desc

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
