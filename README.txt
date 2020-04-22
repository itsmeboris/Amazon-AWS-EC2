security:
    each Instance only starts with a user key pair and doesn't send them to any instance.
    the buckets are account bound and are read only and doesn't contain any private information about the user.

scalability:
    this program can be scaled to more clients, it supports how many messages it will receive,
    since it first checks if it received a message and post all of the input to the workers, they can start working while the manager will continue to send more work,
    it then reads and summarizes all of the responses. it would be better to run 3 threads instead of a single one, but more on this in the threads section.

persistence:
    if a node dies, it can be re run using reboot, all instance start with a script that downloads all files needed to run and runs them.
    is a message was sent to the SQS and the manager died, we can re run it and still read the messages because they are only deleted
    if there are no more tasks to be done with the message.

threads:
    it would be a good idea to give the manager 3 threads one that reads from the local application queue, one that sends messages to the workers
    and one that reads from the workers, to ensure effective usage of the CPU.
    in this project we didn't use any threads because we are running on weak computers that can't handle many threads.

multiple applications:
    the project supports multiple application, it will output to the queue the response with each clients unique id.
    once it receives the termination command it will first finish all of its work, turn off all of the workers, delete it's queues,
    and then it will send a message that it was terminated to the application output queue to let the application know it was shut down.
    after the termination request was received no more new jobs from clients will be received.

limitation:
    since we are running on very weak computers we didn't implement threads to the system due to CPU limits.

resource usage:
    both the manager and the workers are working almost all the time
    the workers continue to pull messages from the queue and work on them,
    while the manager is constantly checking for new messages in it's input and output queues.
    the local application is working a little less and each time goes to sleep for 2 minutes if the task wasn't done,
    this is inorder to not disturb the user and allowing him to work on more things in the meantime.

distribution:
    in the system the task received in from the local application is being distributed to workers each running a small tasks
    but all are simultaneous and are not awaiting one another and with a manager that aggregates all of the results and
    sends them out to the applications once they are finished. none of the applications is affected from one to another.
 

Questions about visibility and queues:
make sure they didn't set visibility to the same value everywhere without thinking about it!
In our implementation we used 4 queues: 
output/input queue for manager-application communication
and an output/input queue for manager-workers communication 
we set visibility value to 0 in all queues except for the manager-worker input queue, the one that the manager uses to pass messages to workers which is 30 secs. The goal was to prevent two workers working on the same message, and on the other hand since each application has it's own queues it makes sense to set the value for the rest of the queues to 0. since at any given time a message is consumed by no more than one entity. 

What is in-flight-mode?
in-flight-mode is a when a message received from a consumer but has not yet deleted from the queue. 

You have 2 apps connecting to the manager, both sent task requests and are waiting for an answer. The manager uses a single queue to write answers to apps. The manager posted 2 answers in the queue. How would you play with visibility time to make your program as efficient as possible?
I would set visibility to a low number value like 3 secs, so an application can verify the message is intended to it. 


What happens if you delete a message immediately after you take from the queue? Two scenarios: 1) worker takes a message deletes it from the queue processes it and returns an answer. 2) Local app takes a message from the answer queue deletes it and downloads the file.
In scenerio one there's no guarentee that the message was processed succesfully in case the worker crashed for some reason.. and that deleted message won't be restored . 


Questions about memory:
Lets say the manager saves the result of each message in its memory , is such a solution scalable? How would you solve it? (write to files buffers of reviews once it reaches a certain size)
No such a solution is no scalable, since the manager can crash while saving the result of some message. I would use S3 service to store all the result messages. 

