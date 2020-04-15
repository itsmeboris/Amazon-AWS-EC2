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