# -*- coding: utf-8 -*-
"""
Created on Mon Jun 06 03:07:06 2016

@author: Jason
"""

import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict

""" SAMPLE DATA
Model 1-Index Construction Time-13942
Model 1-Entries in File-226840
Model 1-Index Construction Time-14393
Model 1-Entries in File-226840
Model 1-Index Construction Time-13838
Model 1-Entries in File-226840
Model 1-Index Construction Time-12945
Model 1-Entries in File-226840
Model 1-Query Time-ibm research report-6254
Model 1-Search Time-ibm research report-6741
Model 1-Query Time-ibm research report-4611
Model 1-Search Time-ibm research report-5038
Model 1-Query Time-ibm research report-4914
Model 1-Search Time-ibm research report-8272
Model 1-Query Time-ibm research report-4528
Model 1-Search Time-ibm research report-4868
"""

"""
Plots to make:
Graph of system x time it takes to search
Graph of system x query process time
Graph of system x num entries stored
Graph of system x time to construct index file

----------------------

Graph of search time:
List of search queries
Mapping of query to a list of model's values
    This will be later averaged to a single value, to be put into a list
    
Graph of Index Time:
Doesn't have multiple quries to worry about.  Only needs one set of bars
Collect list of data 

Graph of Entry No.
"""

#Dictionary lookup for shorter names
query_to_short = {'ibm research report':'IRR', 'network':'N'}


"""
Search Time Containers
"""
#Container to hold queries for search time
search_queries = []

#Container to hold the values of models after they've been averaged
model_1_search_values = []
model_2_search_values = []
model_3_search_values = []
model_4_search_values = []
model_1_search_stds = []
model_2_search_stds = []
model_3_search_stds = []
model_4_search_stds = []

#Container to hold a list of values mapped to the query 
model_1_search_dict = defaultdict(list)
model_2_search_dict = defaultdict(list)
model_3_search_dict = defaultdict(list)
model_4_search_dict = defaultdict(list)


"""
Cloud Search Time Containers
"""
#Container to hold queries
cloud_search_queries = []

#Values after they've been averaged
model_1_cloud_search_values = []
model_2_cloud_search_values = []
model_3_cloud_search_values = []
model_4_cloud_search_values = []
model_1_cloud_search_stds = []
model_2_cloud_search_stds = []
model_3_cloud_search_stds = []
model_4_cloud_search_stds = []

#Container to hold list of values mapped to the query
model_1_cloud_search_dict = defaultdict(list)
model_2_cloud_search_dict = defaultdict(list)
model_3_cloud_search_dict = defaultdict(list)
model_4_cloud_search_dict = defaultdict(list)

"""
Query Processing Time Containers
"""
#Container for queries for query processing.  Yeah I know this is redundant but yeah.
query_queries = []

#Container to hold the values of models
model_1_query_values = []
model_2_query_values = []
model_3_query_values = []
model_4_query_values = []
model_1_query_stds = []
model_2_query_stds = []
model_3_query_stds = []
model_4_query_stds = []

#Container to hold lists of values mapped to the query
model_1_query_dict = defaultdict(list)
model_2_query_dict = defaultdict(list)
model_3_query_dict = defaultdict(list)
model_4_query_dict = defaultdict(list)
    

"""
Index Processing Time Containers
"""
model_1_index_values = []
model_2_index_values = []
model_3_index_values = []
model_4_index_values = []
model_1_index_stds = []
model_2_index_stds = []
model_3_index_stds = []
model_4_index_stds = []


"""
Index Entry Number Containers
"""
index_entry_values = {}


"""
PROCESS FILE
"""

f = open('search_metrics.txt')
for line in f:
    tokens = line.split('-')
    
    #Switch based on its second value, determining the type of metric this is
    if tokens[1] == "Search Time":
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        #Only add query if it's not already in the queries
        if query not in search_queries:
            search_queries.append(query)
        
        #Add in value data to the right model container
        if model == "SmartShards":
            model_1_search_dict[query].append(value)
        elif model == "Model 2":
            model_2_search_dict[query].append(value)
        elif model == "Model 3":
            model_3_search_dict[query].append(value)
        elif model == "KSWF":
            model_4_search_dict[query].append(value)
            
    elif tokens[1] == "Cloud Search Time":
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        if query not in cloud_search_queries:
            cloud_search_queries.append(query)
            
        #Add in value data to the right model's container
        if model == "SmartShards":
            model_1_cloud_search_dict[query].append(value)
        elif model == "Model 2":
            model_2_cloud_search_dict[query].append(value)
        elif model == "Model 3":
            model_3_cloud_search_dict[query].append(value)
        elif model == "KSWF":
            model_4_cloud_search_dict[query].append(value)
    
    elif tokens[1] == "Query Time":
        model = tokens[0]
        query = tokens[2]
        value = eval(tokens[3])
        
        #Add query if it's not in
        if query not in query_queries:
            query_queries.append(query)
        
        #Add in value data
        if model == "SmartShards":
            model_1_query_dict[query].append(value)
        elif model == "Model 2":
            model_2_query_dict[query].append(value)
        elif model == "Model 3":
            model_3_query_dict[query].append(value)
        elif model == "KSWF":
            model_4_query_dict[query].append(value)
            
    elif tokens[1] == "Index Construction Time":
        model = tokens[0]
        value = eval(tokens[2])
        
        if model == "SmartShards":
            model_1_index_values.append(value)
        elif model == "Model 2":
            model_2_index_values.append(value)
        elif model == "Model 3":
            model_3_index_values.append(value)
        elif model == "KSWF":
            model_4_index_values.append(value)
            
    elif tokens[1] == "Entries in File":
        model = tokens[0]
        value = eval(tokens[2])
        
        if model == "SmartShards":
            index_entry_values["Model 1"] = value
        elif model == "Model 2":
            index_entry_values["Model 2"] = value
        elif model == "Model 3":
            index_entry_values["Model 3"] = value
        elif model == "KSWF":
            index_entry_values["KSWF"] = value


"""
Search Time Plot
"""
#Get the means and stds into their proper containers
for query in search_queries:
    model_1_search_values.append(np.mean(model_1_search_dict[query]) / 1000)
    model_2_search_values.append(np.mean(model_2_search_dict[query]) / 1000)
    model_3_search_values.append(np.mean(model_3_search_dict[query]) / 1000)
    model_4_search_values.append(np.mean(model_4_search_dict[query]) / 1000)
    model_1_search_stds.append(np.std(model_1_search_dict[query]) / 1000)
    model_2_search_stds.append(np.std(model_2_search_dict[query]) / 1000)
    model_3_search_stds.append(np.std(model_3_search_dict[query]) / 1000)
    model_4_search_stds.append(np.std(model_4_search_dict[query]) / 1000)
    
temp_queries = search_queries
temp_queries[:] = [x.replace('ibm', 'IBM') for x in search_queries]

search_queries = temp_queries
    
print model_1_search_values
print model_3_search_stds
print search_queries

num_groups = len(search_queries)

fig, ax = plt.subplots()

index = np.arange(num_groups)
bar_width = 0.2

opacity = 0.4
error_config = {'ecolor': '0.3'}

rects1 = plt.bar(index, model_1_search_values, bar_width,
                 alpha=opacity,
                 color='b',
                 yerr=model_1_search_stds,
                 error_kw=error_config,
                 label='SNSS',
                 hatch='-')

rects2 = plt.bar(index + bar_width, model_2_search_values, bar_width,
                 alpha=opacity,
                 color='r',
                 yerr=model_2_search_stds,
                 error_kw=error_config,
                 label='FKSS',
                 hatch='//')
                 
rects3 = plt.bar(index + bar_width + bar_width, model_3_search_values, bar_width,
                 alpha=opacity,
                 color='g',
                 yerr=model_3_search_stds,
                 error_kw=error_config,
                 label='SKSS',
                 hatch='o')
                 
rects4 = plt.bar(index + bar_width + bar_width + bar_width, model_4_search_values, bar_width,
                 alpha=opacity,
                 color='pink',
                 yerr=model_4_search_stds,
                 error_kw=error_config,
                 label='KSWF')
                 
                 

plt.xlabel('Query', fontsize=13)
plt.ylabel('Total Search Time (s)', fontsize=13)
plt.xticks(index + bar_width + .2, search_queries)
ax.set_position([0.12,-0.0,0.5,0.8])
plt.legend(loc='lower center', bbox_to_anchor=(0.5, -.3),
          fancybox=True, shadow=True, ncol=5)

plt.tight_layout()

plt.savefig('search_time_fig.png', dpi=200)

plt.show()


"""
Cloud Search Time Plot
"""
#Get the means and stds into their proper containers
for query in cloud_search_queries:
    model_1_cloud_search_values.append(np.mean(model_1_cloud_search_dict[query]) / 1000)
    model_2_cloud_search_values.append(np.mean(model_2_cloud_search_dict[query]) / 1000)
    model_3_cloud_search_values.append(np.mean(model_3_cloud_search_dict[query]) / 1000)
    model_4_cloud_search_values.append(np.mean(model_4_cloud_search_dict[query]) / 1000)
    model_1_cloud_search_stds.append(np.std(model_1_cloud_search_dict[query]) / 1000)
    model_2_cloud_search_stds.append(np.std(model_2_cloud_search_dict[query]) / 1000)
    model_3_cloud_search_stds.append(np.std(model_3_cloud_search_dict[query]) / 1000)
    model_4_cloud_search_stds.append(np.std(model_4_cloud_search_dict[query]) / 1000)

temp_queries = cloud_search_queries
temp_queries[:] = [x.replace('ibm', 'IBM') for x in search_queries]

cloud_search_queries = temp_queries

print model_1_cloud_search_values
print model_1_cloud_search_stds
print search_queries

num_groups = len(search_queries)

fig, ax = plt.subplots()

index = np.arange(num_groups)
bar_width = 0.2

opacity = 0.4
error_config = {'ecolor': '0.3'}

rects1 = plt.bar(index, model_1_cloud_search_values, bar_width,
                 alpha=opacity,
                 color='b',
                 yerr=model_1_cloud_search_stds,
                 error_kw=error_config,
                 label='SNSS',
                 hatch='-')

rects2 = plt.bar(index + bar_width, model_2_cloud_search_values, bar_width,
                 alpha=opacity,
                 color='r',
                 yerr=model_2_cloud_search_stds,
                 error_kw=error_config,
                 label='FKSS',
                 hatch='//')
                 
rects3 = plt.bar(index + bar_width + bar_width, model_3_cloud_search_values, bar_width,
                 alpha=opacity,
                 color='g',
                 yerr=model_3_cloud_search_stds,
                 error_kw=error_config,
                 label='SKSS',
                 hatch='o')
                 
rects3 = plt.bar(index + bar_width + bar_width + bar_width, model_4_cloud_search_values, bar_width,
                 alpha=opacity,
                 color='pink',
                 yerr=model_4_cloud_search_stds,
                 error_kw=error_config,
                 label='KSWF')
                 
plt.xlabel('Query', fontsize=13)
plt.ylabel('Search time on Hashed Index (s)', fontsize=13)
plt.xticks(index + bar_width + .2, cloud_search_queries)
plt.ylim((0, 3.5))
ax.set_position([0.12,-0.0,0.5,0.8])
plt.legend(loc='lower center', bbox_to_anchor=(0.5, -.3),
          fancybox=True, shadow=True, ncol=5)
          
#ax.set_yscale('log')

plt.tight_layout()

plt.savefig('cloud_search_time_fig.png', dpi=200)

plt.show()

"""
Query Time Plot
"""
for query in query_queries:
    model_1_query_values.append(np.mean(model_1_query_dict[query]) / 1000)
    model_2_query_values.append(np.mean(model_2_query_dict[query]) / 1000)
    model_3_query_values.append(np.mean(model_3_query_dict[query]) / 1000)
    model_4_query_values.append(np.mean(model_4_query_dict[query]) / 1000)
    model_1_query_stds.append(np.std(model_1_query_dict[query]) / 1000)
    model_2_query_stds.append(np.std(model_2_query_dict[query]) / 1000)
    model_3_query_stds.append(np.std(model_3_query_dict[query]) / 1000)
    model_4_query_stds.append(np.std(model_4_query_dict[query]) / 1000)

temp_queries = query_queries
temp_queries[:] = [x.replace('ibm', 'IBM') for x in search_queries]

query_queries = temp_queries

print model_1_query_values
print model_1_query_stds
print query_queries

num_groups = len(query_queries)

fig, ax = plt.subplots()

index = np.arange(num_groups)
bar_width = 0.2

opacity = 0.4
error_config = {'ecolor': '0.3'}

rects1 = plt.bar(index, model_1_query_values, bar_width,
                 alpha=opacity,
                 color='b',
                 yerr=model_1_query_stds,
                 error_kw=error_config,
                 label='SNSS',
                 hatch='-')

rects2 = plt.bar(index + bar_width, model_2_query_values, bar_width,
                 alpha=opacity,
                 color='r',
                 yerr=model_2_query_stds,
                 error_kw=error_config,
                 label='FKSS',
                 hatch='//')
                 
rects3 = plt.bar(index + bar_width + bar_width, model_3_query_values, bar_width,
                 alpha=opacity,
                 color='g',
                 yerr=model_3_query_stds,
                 error_kw=error_config,
                 label='SKSS', hatch='o')
                 
rects4 = plt.bar(index + bar_width + bar_width + bar_width, model_4_query_values, bar_width,
                 alpha=opacity,
                 color='pink',
                 yerr=model_4_query_stds,
                 error_kw=error_config,
                 label='KSWF')
                 

plt.xlabel('Query', fontsize=13)
plt.ylabel('Query Processing Time (s)', fontsize=13)
plt.xticks(index + bar_width + .2, search_queries)
ax.set_position([0.12,-0.0,0.5,0.8])
plt.legend(loc='lower center', bbox_to_anchor=(0.5, -.3),
          fancybox=True, shadow=True, ncol=5)

plt.tight_layout()

plt.savefig('query_time_fig.png', dpi=200)

plt.show()


"""
Index Construction Time Plot
"""

print model_1_index_values

num_groups = 4

fig, ax = plt.subplots()

index = np.arange(num_groups)
bar_width = 0.4

opacity = 0.4
error_config = {'ecolor': '0.3'}

#Collect data into one list
index_time_values = [np.mean(model_1_index_values) / 1000, np.mean(model_2_index_values) / 1000, np.mean(model_3_index_values) / 1000, np.mean(model_4_index_values) / 1000]
index_time_stds = [np.std(model_1_index_values) / 1000, np.std(model_2_index_values) / 1000, np.std(model_3_index_values) / 1000, np.std(model_4_index_values) / 1000]

print index_time_values

rects1 = plt.bar(index, index_time_values, bar_width,
                 alpha=opacity,
                 color='b',
                 label='Model 1')
                 

plt.xlabel('Systems', fontsize=13)
plt.ylabel('Index Construction Time (s)', fontsize=13)
plt.xticks(index + bar_width -.16, ['SNSS','FKSS','SKSS','KSWF'])
#plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.05),
          #fancybox=True, shadow=True, ncol=5)

#ax.set_yscale('log')

plt.tight_layout()

plt.savefig('index_time_fig.pdf')

plt.show()


"""
Number of Entries in Hashed Index Plot
Must be set up somewhat differently.  Each model will get its own separate bar
"""

print index_entry_values

num_groups = 4

fig, ax = plt.subplots()

index = np.arange(num_groups)
bar_width = 0.4

opacity = 0.4
error_config = {'ecolor': '0.3'}

#Extract the data from the map we set up
index_entry_list = []
index_entry_list.append(index_entry_values['Model 1'])
index_entry_list.append(index_entry_values['Model 2'])
index_entry_list.append(index_entry_values['Model 3'])
index_entry_list.append(index_entry_values['KSWF'])

index_entry_model_names = ['SNSS', 'FKSS', 'SKSS', 'KSWF']

rects1 = plt.bar(index, index_entry_list, bar_width,
                 alpha=opacity,
                 color='b',
                 label='Model 1')
                 

plt.xlabel('System', fontsize=13)
plt.ylabel('Number of Entries in Hashed Index', fontsize=13)
plt.xticks(index + bar_width - .16, index_entry_model_names)

plt.tight_layout()

plt.savefig('entries_fig.pdf')

plt.show()