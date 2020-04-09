# AMAZON-AWS-EC2-S3-SQS-WORKER MANAGER
## PDF Document Conversion in the Cloud
### Assignment for course DSP202

In this assignment I will code a real-world application to distributively process a list of PDF files, perform some operations on them, and display the result on a web page. 

The application is composed of a local application and instances running on the Amazon cloud. The application will get as an input a text file containing a list of URLs of PDF files with an operation to perform on them. Then, instances will be launched in AWS (workers). Each worker will download PDF files, perform the requested operation, and display the result of the operation on a webpage.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.  
See deployment for notes on how to deploy the project on a live system.
> You can build this jar using the pom file provided or just by downloading the jar Ex1.jar.

### Prerequisites

* Java 1.8 and higher  
* Maven
* AMAZON AWS IAM Role that has:
  * AdministratorAccess
  * AmazonEC2FullAccess
  * AmazonSQSFullAccess
  * AmazonS3FullAccess
* AMAZON aws Security group that has inbound and outbound network enabled
* Key pair to give your running manager to pass to new instances (this is why AdministratorAccess is required)  
  
<b>please create those profile and change their names inside awsVars.java


## Deployment

* To Run local application simply download the Ex1.jar and then run it using  
> java -jar *jarname*.jar input-file output-file number-of-workers terminate b  
trminate and b are optional terminate will tell the manager to terminate once finished and b <b> will delete all buckets before running.
* To run Manager application run:
> java -cp Ex1.jar Ex1.Manager
* To run Worker application run:
> java -cp Ex1.jar Ex1.Worker

## Built With
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Boris Sobol**

## License

This project is licensed under the Apache License - see the [LICENSE.md](LICENSE.md) file for details
