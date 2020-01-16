import pandas as pd
import numpy as np
import math
import os
from scipy.special import entr

csvFolderPath = "../../../COIN/4_Repos/Ready_For_Regression/"
resultPath = '../Regression/input.csv'


def giniCalculator(array):
    x = np.array(array)
    mad = np.abs(np.subtract.outer(x, x)).mean()
    # Relative mean absolute difference
    rmad = mad / np.mean(x)
    # Gini coefficient
    g = 0.5 * rmad
    g = round(g,4)
    return g

# Function used to extract aggregated metrics from 1 repository
def processCSV(dir, df):
    actorPath = csvFolderPath + dir + '/processed/nodes.csv'
    ticketsPath = csvFolderPath + dir + '/processed/edges.csv'
    SOTPath = csvFolderPath + dir + '/processed/sentiment_over_time.csv'
    activityPath = csvFolderPath + dir + '/processed/activity.csv'
    awvciPath = csvFolderPath + dir + '/processed/awvci.csv'
    betwCentrPath = csvFolderPath + dir + '/processed/betw_centr.csv'
    betwOscPath = csvFolderPath + dir + '/processed/betw_osc.csv'
    densityPath = csvFolderPath + dir + '/processed/density.csv'

    repo = dir
    # Extract metrics from actor file
    actors = pd.read_csv(actorPath, sep=',', encoding='utf-8', error_bad_lines=False)
    actors_rows = actors.shape[0]
    ntop = int(round((actors_rows * 0.05), 0))

    actorsTop = actors.sort_values(by='total influence', ascending=False).head(ntop)

    top_avg_deg_cent = round(actorsTop['Degree centrality'].mean(), 4)
    top_gini_deg_cent = giniCalculator(actorsTop['Degree centrality'])
    top_avg_betw_osc = round(actorsTop['Betweenness centrality oscillation'].mean(), 4)
    top_gini_betw_osc = giniCalculator(actorsTop['Betweenness centrality oscillation'])
    top_avg_sentiment = round(actorsTop['avg sentiment'].mean(), 4)
    top_gini_sentiment = giniCalculator(actorsTop['avg sentiment'])
    top_avg_complexity = round(actorsTop['avg complexity'].mean(), 4)
    top_gini_complexity = giniCalculator(actorsTop['avg complexity'])
    top_avg_influence = round(actorsTop['total influence'].mean(), 4)
    top_gini_influence = giniCalculator(actorsTop['total influence'])
    top_avg_influence_pm = round(actorsTop['average influence per message'].mean(), 4)
    top_gini_influence_pm = giniCalculator(actorsTop['average influence per message'])
    top_avg_contrib = round(actorsTop['Contribution index'].mean(), 4)
    top_gini_contrib = giniCalculator(actorsTop['Contribution index'])
    top_avg_contrib_oscil = round(actorsTop['Contribution index oscillation'].mean(), 4)
    top_gini_contrib_oscil = giniCalculator(actorsTop['Contribution index oscillation'])

    actorsConnected = actors.loc[actors['Degree centrality'] > 1]
    avg_deg_cent = round(actorsConnected['Degree centrality'].mean(), 4)
    gini_deg_cent = giniCalculator(actorsConnected['Degree centrality'])
    avg_betw_osc = round(actorsConnected['Betweenness centrality oscillation'].mean(), 4)
    gini_betw_osc = giniCalculator(actorsConnected['Betweenness centrality oscillation'])
    avg_influence = round(actorsConnected['total influence'].mean(), 4)
    gini_influence = giniCalculator(actorsConnected['total influence'])
    avg_influence_pm = round(actorsConnected['average influence per message'].mean(), 4)
    gini_influence_pm = giniCalculator(actorsConnected['average influence per message'])
    avg_contrib = round(actorsConnected['Contribution index'].mean(), 4)
    # gini_contrib = giniCalculator(actorsConnected['Contribution index'])
    avg_contrib_oscil = round(actorsConnected['Contribution index oscillation'].mean(), 4)
    gini_contrib_oscil = giniCalculator(actorsConnected['Contribution index oscillation'])
    avg_sentiment = round(actorsConnected['avg sentiment'].mean(), 4)
    gini_sentiment = giniCalculator(actorsConnected['avg sentiment'])
    avg_complexity = round(actorsConnected['avg complexity'].mean(), 4)
    gini_complexity = giniCalculator(actorsConnected['avg complexity'])

    perc_connected = round((actors.loc[actors['Degree centrality'] >= ntop].shape[0]) / actors_rows, 4)
    perc_isolated = round((actors.loc[actors['Degree centrality'] == 1].shape[0]) / actors_rows, 4)
    perc_hirable = round((actors.loc[actors['hireable'] == 1].shape[0]) / actors_rows, 4)

    # Extract metrics from ticket file

    edges = pd.read_csv(ticketsPath, sep=',', encoding='utf-8', error_bad_lines=False)

    edges_rows = edges.shape[0]
    perc_closed_issues = round((edges.loc[edges['Status'] == 'closed'].shape[0]) / edges_rows, 4)
    perc_creation = round((edges.loc[edges['Edge_type'] == 'CREATION'].shape[0]) / edges_rows, 4)
    perc_solo = edges[['Name']].groupby('Name').size().reset_index(name='counts')
    perc_solo = round((perc_solo.loc[perc_solo['counts'] == 1].shape[0]) / perc_solo.shape[0], 4)

    # Extract metrics from sentiment over time data

    SOT = pd.read_csv(SOTPath, sep=',', encoding='utf-8', error_bad_lines=False)

    group_messages = round(SOT['Avg. messages per day'].mean(), 4)
    group_sentiment = round(SOT['Sentiment'].mean(), 4)
    group_emotionality = round(SOT['Emotionality'].mean(), 4)
    group_complexity = round(SOT['Complexity'].mean(), 4)
    group_influence = round(SOT['Influence'].mean(), 4)

    # Extract time series data

    activity = pd.read_csv(activityPath, sep=',', encoding='utf-8', error_bad_lines=False).T[[0]]
    activity['Activity_Increase'] = np.nan
    activity = activity[2:].reset_index()
    for index, row in activity.iterrows():
        if index > 0:
            activity.loc[index, 'Activity_Increase'] = activity.loc[index, 0] / activity.loc[index - 1, 0]
    group_activity_increase = round(activity['Activity_Increase'].mean(), 4)
    if math.isnan(group_activity_increase):
        group_activity_increase = 1
    group_activity = activity.loc[:,0]
    group_activity = round(group_activity.tail(1).values[0],4)

    awvci = pd.read_csv(awvciPath, sep=',', encoding='utf-8', error_bad_lines=False).T[[0]]
    awvci['awvci_Increase'] = np.nan
    awvci = awvci[2:].reset_index()
    for index, row in awvci.iterrows():
        if index > 0:
            if awvci.loc[index - 1, 0] > 0:
                awvci.loc[index, 'awvci_Increase'] = awvci.loc[index, 0] / awvci.loc[index - 1, 0]
    group_awvci_increase = round(awvci['awvci_Increase'].mean(), 4)
    if math.isnan(group_awvci_increase):
        group_awvci_increase = 1
    group_awvci = awvci.loc[:,0]
    group_awvci = round(group_awvci.tail(1).values[0],4)

    betwCentr = pd.read_csv(betwCentrPath, sep=',', encoding='utf-8', error_bad_lines=False).T[[0]]
    betwCentr['betwCentr_Increase'] = np.nan
    betwCentr = betwCentr[2:].reset_index()
    for index, row in betwCentr.iterrows():
        if index > 0:
            if betwCentr.iloc[index - 1, 1] > 0:
                betwCentr.loc[index, 'betwCentr_Increase'] = betwCentr.iloc[index, 1] / betwCentr.iloc[index - 1, 1]
    group_betwCentr_increase = round(betwCentr['betwCentr_Increase'].mean(), 4)
    if math.isnan(group_betwCentr_increase):
        group_betwCentr_increase = 1
    group_betw_centr = betwCentr.loc[:,0]
    group_betw_centr = round(group_betw_centr.tail(1).values[0],4)

    betwOsc = pd.read_csv(betwOscPath, sep=',', encoding='utf-8', error_bad_lines=False).T[[0]]
    betwOsc['betwOsc_Increase'] = np.nan
    betwOsc = betwOsc[2:].reset_index()
    for index, row in betwOsc.iterrows():
        if index > 0:
            if betwOsc.loc[index - 1, 0] > 0:
                betwOsc.loc[index, 'betwOsc_Increase'] = betwOsc.loc[index, 0] / betwOsc.loc[index - 1, 0]
    group_betwOsc_increase = round(betwOsc['betwOsc_Increase'].mean(), 4)
    if math.isnan(group_betwOsc_increase):
        group_betwOsc_increase = 1
    group_betw_osc = betwOsc.loc[:,0]
    group_betw_osc = round(group_betw_osc.tail(1).values[0],4)

    density = pd.read_csv(densityPath, sep=',', encoding='utf-8', error_bad_lines=False).T[[0]]
    density['density_Increase'] = np.nan
    density = density[2:].reset_index()
    for index, row in density.iterrows():
        if index > 0:
            if density.loc[index - 1, 0] > 0:
                density.loc[index, 'density_Increase'] = density.loc[index, 0] / density.loc[index - 1, 0]
    group_density_increase = round(density['density_Increase'].mean(), 4)
    if math.isnan(group_density_increase):
        group_density_increase = 1
    group_density = density.loc[:,0]
    group_density = round(group_density.tail(1).values[0],4)

    result = df.append({'Group_Messages_Per_Day': group_messages,
                        'Group_Sentiment': group_sentiment,
                        'Group_Emotionality': group_emotionality,
                        'Group_Complexity': group_complexity,
                        'Group_Influence': group_influence,
                        'Group_Percentage_Activity_Increase_Monthly': group_activity_increase,
                        'Group_Activity': group_activity,
                        'Group_Percentage_AWVCI_Increase_Monthly': group_awvci_increase,
                        'Group_AWVCI':group_awvci,
                        'Group_Percentage_Betweenness_Centrality_Increase_Monthly': group_betwCentr_increase,
                        'Group_Betweenness_Centrality':group_betw_centr,
                        'Group_Percentage_Betweenness_Oscillation_Increase_Monthly': group_betwOsc_increase,
                        'Group_Betweenness_Oscillation':group_betw_osc,
                        'Group_Percentage_Density_Increase_Monthly': group_density_increase,
                        'Group_Density':group_density,
                        'Avg_Degree_Centrality_Top': top_avg_deg_cent,
                        'Gini_Degree_Centrality_Top': top_gini_deg_cent,
                        'Avg_Degree_Centrality': avg_deg_cent,
                        'Gini_Degree_Centrality': gini_deg_cent,
                        'Avg_Betweenness_Osc_Top': top_avg_betw_osc,
                        'Gini_Betweenness_Osc_Top': top_gini_betw_osc,
                        'Avg_Betweenness_Osc': avg_betw_osc,
                        'Gini_Betweenness_Osc': gini_betw_osc,
                        'Percentage_Connected_Actors': perc_connected,
                        'Avg_Sentiment': avg_sentiment,
                        'Gini_Sentiment': gini_sentiment,
                        'Avg_Sentiment_Top': top_avg_sentiment,
                        'Gini_Sentiment_Top': top_gini_sentiment,
                        'Avg_complextiy': avg_complexity,
                        'Gini_complextiy': gini_complexity,
                        'Avg_complexity_Top': top_avg_complexity,
                        'Gini_complexity_Top': top_gini_complexity,
                        'Avg_Influence_Top': top_avg_influence,
                        'Gini_Influence_Top': top_gini_influence,
                        'Avg_Influence': avg_influence,
                        'Gini_Influence': gini_influence,
                        'Avg_Influence_Per_Message_Top': top_avg_influence_pm,
                        'Gini_Influence_Per_Message_Top': top_gini_influence_pm,
                        'Avg_Influence_Per_Message': avg_influence_pm,
                        'Gini_Influence_Per_Message': gini_influence_pm,
                        'Avg_Contribution_Index_Top': top_avg_contrib,
                        'Gini_Contribution_Index_Top': top_gini_contrib,
                        'Avg_Contribution_Index': avg_contrib,
                        'Avg_Contribution_Index_Oscil_Top': top_avg_contrib_oscil,
                        'Gini_Contribution_Index_Oscil_Top': top_gini_contrib_oscil,
                        'Avg_Contribution_Index_Oscil': avg_contrib_oscil,
                        'Gini_Contribution_Index_Oscil': gini_contrib_oscil,
                        'Percentage_Closed_Issues': perc_closed_issues,
                        'Percentage_Creations': perc_creation,
                        'Percentage_Isolated_Actors': perc_isolated,
                        'Percentage_Hirable_Actors': perc_hirable,
                        'Percentage_Solo_Issues': perc_solo,
                        'Repository_Name': repo}, ignore_index=True)
    return result


# 1: Create DataFrame
df = pd.DataFrame(columns=['Group_Messages_Per_Day',
                           'Group_Sentiment',
                           'Group_Emotionality',
                           'Group_Complexity',
                           'Group_Influence',
                           'Group_Percentage_Activity_Increase_Monthly',
                           'Group_Activity',
                           'Group_Percentage_AWVCI_Increase_Monthly',
                           'Group_AWVCI',
                           'Group_Percentage_Betweenness_Centrality_Increase_Monthly',
                           'Group_Betweenness_Centrality',
                           'Group_Percentage_Betweenness_Oscillation_Increase_Monthly',
                           'Group_Betweenness_Oscillation',
                           'Group_Percentage_Density_Increase_Monthly',
                           'Group_Density',
                           'Avg_Degree_Centrality_Top',
                           'Gini_Degree_Centrality_Top',
                           'Avg_Degree_Centrality',
                           'Gini_Degree_Centrality',
                           'Avg_Betweenness_Osc_Top',
                           'Gini_Betweenness_Osc_Top',
                           'Avg_Betweenness_Osc',
                           'Gini_Betweenness_Osc',
                           'Percentage_Connected_Actors',
                           'Avg_Sentiment',
                           'Gini_Sentiment',
                           'Avg_Sentiment_Top',
                           'Gini_Sentiment_Top',
                           'Avg_complextiy',
                           'Gini_complextiy',
                           'Avg_complexity_Top',
                           'Gini_complexity_Top',
                           'Avg_Influence_Top',
                           'Gini_Influence_Top',
                           'Avg_Influence',
                           'Gini_Influence',
                           'Avg_Influence_Per_Message_Top',
                           'Gini_Influence_Per_Message_Top',
                           'Avg_Influence_Per_Message',
                           'Gini_Influence_Per_Message',
                           'Avg_Contribution_Index_Top',
                           'Gini_Contribution_Index_Top',
                           'Avg_Contribution_Index',
                           'Avg_Contribution_Index_Oscil_Top',
                           'Gini_Contribution_Index_Oscil_Top',
                           'Avg_Contribution_Index_Oscil',
                           'Gini_Contribution_Index_Oscil',
                           'Percentage_Isolated_Actors',
                           'Percentage_Hirable_Actors',
                           'Percentage_Closed_Issues',
                           'Percentage_Creations',
                           'Percentage_Solo_Issues',
                           'Repository_Name',
                           'Target'])

# 2: Process Input CSVs

for dirs in os.listdir(csvFolderPath):
    print(dirs)
    df = processCSV(dirs, df)
df = df.sort_values(by='Repository_Name', ascending=True)
# drop columns with null values
df = df.dropna(axis=1)
# drop columns which contain the string Avg in the header
df = df[df.columns.drop(list(df.filter(regex='Avg')))]
df.to_csv(resultPath, sep=',', encoding='utf-8', index=False)

print(1)
