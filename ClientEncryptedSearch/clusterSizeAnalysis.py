# -*- coding: utf-8 -*-
"""
Created on Sat Apr 08 21:09:30 2017

Args:
1 - number of gigs
2 - number of clusters

The cluster file system should be set up like cloud/xgigs/utilities/yclusters

The names of the cluster choices docs should be set up like ClusterChoices-x-y.txt
@author: Jason
"""

import os
import sys
from collections import defaultdict



clusters = []
termCounts = []
fileCounts = []
queries_term_dict = defaultdict(list) #Maps a query to its total term count
queries_files_dict = defaultdict(list) #Maps a query to its total file count

print sys.argv

G = sys.argv[1]
C = sys.argv[2]

# The folder containing the clusters
clusterFolder = "/home/ec2-user/searchserver/cloud/" + G + "gigs/utilities/" + C + "clusters/"

# Open the doc so we can read the cluster choices
fileName = "ClusterChoices-" + G + "-" + C + ".txt"
clusterChoices = open(fileName)

"""
The file will be organized like:
    Query...
    Cluster1
    Cluster2
    Cluster3
    ...
"""


for i in range(5): #5 queries
    query = clusterChoices.readline().rstrip()
    print "Calculating for Query: " + query
    for j in range(3): #3 clusters
        clusterName = clusterChoices.readline().rstrip()
        clusters.append(open(clusterFolder + "cluster_" + clusterName + ".txt", 'rb'))
        print clusters[j].readline().rstrip()
        
        termCounts.append(0)
        fileCounts.append(0)
        
        # Go through the files, parsing hte lines to determine term and file counts
        for line in clusters[j]:
            termCounts[j] = termCounts[j] + 1
            
            # Now split the line and analyze how many files there are
            splitLines = line.split("|.|")
            numFiles = (len(splitLines) - 1) / 2
            fileCounts[j] = fileCounts[j] + 1
        
        print termCounts[j]
        print fileCounts[j]
        
        queries_term_dict[query].append(termCounts[j])
        queries_files_dict[query].append(fileCounts[j])
        
        
    
    # Need to clean all the clusters, term counts, and file counts
    clusters = []
    termCounts = []
    
    
    
clusterChoices.close()
for f in clusters:
    f.close()

# Calculate the totals.  It will be the sum of all clusters from all queries
termCount = 0
for query in queries_term_dict.keys():
    for size in queries_term_dict[query]:
        termCount += size

fileCount = 0
for query in queries_files_dict.keys():
    for size in queries_files_dict[query]:
        fileCount += size
        
print "Total Terms: " + str(termCount)
print "Total FIles: " + str(fileCount)

termAverage = float(termCount) / 15
fileAverage = float(fileCount) / 15

print "Average Terms: " + str(termAverage)
print "Average files: " + str(fileAverage)


outputFileName = "counts-" + G + "G" + C + "C.txt"
outputFile = open(outputFileName, 'w')

print "Now writing this info to file " + outputFileName

outputFile.write(G + "-" + C)
outputFile.write("\n")
outputFile.write("Total Terms-" + str(termCount))
outputFile.write("\n")
outputFile.write("Total Files-" + str(fileCount))
outputFile.write("\n")
outputFile.write("Average Terms-" + str(termAverage) )
outputFile.write("\n")
outputFile.write("Average Files-" + str(fileAverage))

outputFile.flush()
outputFile.close()

"""
for clusterName in clusterChoices:
    clusters.append
    

for i in range(3):
    clusters.append(open(clusterFolder + "cluster_" + sys.argv[i+1] + ".txt"))
    print clusters[i].readline()
    termCounts.append(0)
    
    # Go through the files, parsing the lines to determine term and file counts
    for line in clusters[i]:
        termCounts[i] = termCounts[i] + 1
    
    print termCounts[i]
"""
    