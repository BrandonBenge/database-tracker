# database-tracker

## Purpose
This will be a vary modulized program for development purposes.

## TestFramework
This program is to proof test an HA with LB mysql environment. It will work with both MySQL and MariaDB with focus on MariaDB with Galara. Currently my focus in only on the GSLB/LB stuff. I look to extend further to many different areas.

## Focused environment for our Galara clusters
                  GSLB(Global Load Balancer)
                             / \
                           /     \
                         /         \
                       /             \
                     /                 \
            vipLB(primary)        vipLB(Secondary)
                /|\                      /|\
              /  |  \                  /  |  \
            /    |    \              /    |    \
          /      |      \          /      |      \
      (Maria1  Maria2  Maria3) (Maria4  Maria5  Maria6) - 2 seperate galara clusters with async replication
                           \    /
                     (async replication)
                       
                       
## Use(FYI not tested yet, just noting what it should be)
java -cp java.jar com.benge.database.runTest.TestFramework \<location of json file\> \<database user\> \<database pass\>

## Example of json file
- [json file](resources/file.json)