# -*- coding: utf-8 -*-
"""
Created on Mon Aug 01 09:19:51 2016

@author: Jason
"""

import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict

#hold the queries
search_queries = []

#Hold the values we want for the x axis
sizes = [.5, 1, 5]

#List of lists that will hold 4 lists, one for each query
MB500_values = []
GB1_values = []
GB5_values = []
GB10_values = []
GB25_values = []
GB50_values = []
MB500_stds = []
GB1_stds = []
GB5_stds = []
GB10_stds = []
GB25_stds = []
GB50_stds = []

MB500_dict = defaultdict(list)
GB1_dict = defaultdict(list)
GB5_dict = defaultdict(list)
GB10_dict = defaultdict(list)
GB25_dict = defaultdict(list)
GB50_dict = defaultdict(list)

name = 'search_metrics'
f = open(name + '.txt')

for line in f:
    tokens = line.split('-')
    
    if tokens[1] == 'Search Time':
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        if query not in search_queries:
            search_queries.append(query)
            print query
    
        if model == "500":        
            MB500_dict[query].append(value)
   
        elif model == "1000":
            GB1_dict[query].append(value)
     
        elif model == "1500":
            GB5_dict[query].append(value)
"""
        elif model == "10000":
            GB10_dict[query].append(value)
        elif model == "25000":
            GB25_dict[query].append(value)
        elif model == "50000":
            GB50_dict[query].append(value)
"""
for query in search_queries:
    MB500_values.append(np.mean(MB500_dict[query]))
    MB500_stds.append(np.std(MB500_dict[query]))
    
    GB1_values.append(np.mean(GB1_dict[query]))
    GB1_stds.append(np.std(GB1_dict[query]))

    GB5_values.append(np.mean(GB5_dict[query]))
    GB5_stds.append(np.std(GB5_dict[query]))
"""
    GB10_values.append(np.mean(GB10_dict[query]))
    GB25_values.append(np.mean(GB25_dict[query]))
    GB50_values.append(np.mean(GB50_dict[query]))
    """
    

"""
    GB10_stds.append(np.std(GB10_dict[query]))
    GB25_stds.append(np.std(GB25_dict[query]))
    GB50_stds.append(np.std(GB50_dict[query]))
   """ 
MB500_avg_search = np.mean(MB500_values)

GB1_avg_search = np.mean(GB1_values)

GB5_avg_search = np.mean(GB5_values)
"""
GB10_avg_search = np.mean(GB10_values)
GB25_avg_search = np.mean(GB25_values)
GB50_avg_search = np.mean(GB50_values)
"""

search_averages = [MB500_avg_search / 1000, GB1_avg_search / 1000, GB5_avg_search / 1000]
#search_averages = [MB500_avg_search / 1000, GB1_avg_search / 1000, GB5_avg_search / 1000, GB10_avg_search / 1000, GB25_avg_search / 1000, GB50_avg_search / 1000]

print search_averages
print sizes

f.close()

#Do it all again!!!

MB500_dict.clear()
GB10_dict.clear()
GB1_dict.clear()
GB5_dict.clear()
GB25_dict.clear()
GB50_dict.clear()

MB500_values = []
GB1_values = []
GB5_values = []
GB10_values = []
GB25_values = []
GB50_values = []
MB500_stds = []
GB1_stds = []
GB5_stds = []
GB10_stds = []
GB25_stds = []
GB50_stds = []

f = open(name + '.txt')

for line in f:
    tokens = line.split('-')
    
    if tokens[1] == 'Cloud Time':
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        if query not in search_queries:
            search_queries.append(query)
            print query
    
        if model == "500":        
            MB500_dict[query].append(value)
            
        elif model == "1000":
            GB1_dict[query].append(value)

        elif model == "1500":
            GB5_dict[query].append(value)
"""
        elif model == "10000":
            GB10_dict[query].append(value)
        elif model == "25000":
            GB25_dict[query].append(value)
        elif model == "50000":
            GB50_dict[query].append(value)
"""
for query in search_queries:    
    MB500_values.append(np.mean(MB500_dict[query]))
    MB500_stds.append(np.std(MB500_dict[query]))

    GB1_values.append(np.mean(GB1_dict[query]))
    GB1_stds.append(np.std(GB1_dict[query]))

    GB5_values.append(np.mean(GB5_dict[query]))
    GB5_stds.append(np.std(GB5_dict[query]))
"""
    GB10_values.append(np.mean(GB10_dict[query]))
    GB25_values.append(np.mean(GB25_dict[query]))
    GB50_values.append(np.mean(GB50_dict[query]))
"""   
    

    

    
"""
    GB10_stds.append(np.std(GB10_dict[query]))
    GB25_stds.append(np.std(GB25_dict[query]))
    GB50_stds.append(np.std(GB50_dict[query]))
   """ 
MB500_avg_cloud = np.mean(MB500_values)

GB1_avg_cloud = np.mean(GB1_values)

GB5_avg_cloud = np.mean(GB5_values)
"""
GB10_avg_cloud = np.mean(GB10_values)
GB25_avg_cloud = np.mean(GB25_values)
GB50_avg_cloud = np.mean(GB50_values)
"""

cloud_averages = [MB500_avg_cloud / 1000, GB1_avg_cloud / 1000, GB5_avg_cloud / 1000]
#cloud_averages = [MB500_avg_cloud / 1000, GB1_avg_cloud / 1000, GB5_avg_cloud / 1000, GB10_avg_cloud / 1000, GB25_avg_cloud / 1000, GB50_avg_cloud / 1000]

print sizes


f.close()

#Do it all again!!!

MB500_dict.clear()
GB10_dict.clear()
GB1_dict.clear()
GB5_dict.clear()
GB25_dict.clear()
GB50_dict.clear()
MB500_values = []
GB1_values = []
GB5_values = []
GB10_values = []
GB25_values = []
GB50_values = []
MB500_stds = []


GB1_stds = []
GB5_stds = []
GB10_stds = []
GB25_stds = []
GB50_stds = []

f = open(name + '.txt')

for line in f:
    tokens = line.split('-')
    
    if tokens[1] == 'Query Time':
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        if query not in search_queries:
            search_queries.append(query)
            print query
    
        if model == "500":        
            MB500_dict[query].append(value)
            
        elif model == "1000":
            GB1_dict[query].append(value)

        elif model == "1500":
            GB5_dict[query].append(value)
"""
        elif model == "10000":
            GB10_dict[query].append(value)
        elif model == "25000":
            GB25_dict[query].append(value)
        elif model == "50000":
            GB50_dict[query].append(value)
"""
for query in search_queries:
    MB500_values.append(np.mean(MB500_dict[query]))
    MB500_stds.append(np.std(MB500_dict[query]))
   
    GB1_stds.append(np.std(GB1_dict[query]))
    GB1_values.append(np.mean(GB1_dict[query]))
    
    GB5_values.append(np.mean(GB5_dict[query]))
    GB5_stds.append(np.std(GB5_dict[query]))
"""
    GB10_values.append(np.mean(GB10_dict[query]))
    GB25_values.append(np.mean(GB25_dict[query]))
    GB50_values.append(np.mean(GB50_dict[query]))
"""    
    
    
    

"""    GB10_stds.append(np.std(GB10_dict[query]))
    GB25_stds.append(np.std(GB25_dict[query]))
    GB50_stds.append(np.std(GB50_dict[query]))
    """
    

MB500_avg_query = np.mean(MB500_values)

GB1_avg_query = np.mean(GB1_values)

GB5_avg_query = np.mean(GB5_values)
"""
GB10_avg_query = np.mean(GB10_values)
GB25_avg_query = np.mean(GB25_values)
GB50_avg_query = np.mean(GB50_values)
"""
query_averages = [MB500_avg_query / 1000, GB1_avg_query / 1000, GB5_avg_query / 1000]
#query_averages = [MB500_avg_query / 1000, GB1_avg_query / 1000, GB5_avg_query / 1000, GB10_avg_query / 1000, GB25_avg_query / 1000, GB50_avg_query / 1000]

print query_averages
print sizes

fig = plt.subplot()

plt.plot(sizes, search_averages, marker = 'o', linestyle = '-', color = 'r', label='total search time', linewidth = 2, markersize=10)
plt.plot(sizes, query_averages, marker = '^', linestyle = '--', color = 'b', label='query processing time', linewidth=2, markersize=10)
plt.plot(sizes, cloud_averages, marker = 's', linestyle = ':', color = 'g', label='hashed index search time', linewidth=2, markersize=10)
plt.xlabel('Size of Dataset (GB)', fontsize=13)
plt.ylabel('Search Time (s)', fontsize=13)
plt.legend(bbox_to_anchor=(1, 0.6))

plt.savefig('scalable_metrics_fig.png')

plt.show()



f.close()

#ratio = GB50_avg_search / MB500_avg_search 
print ratio
print 50000 / 500