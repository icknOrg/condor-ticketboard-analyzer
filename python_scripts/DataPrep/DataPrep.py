import pandas as pd
import os

csvFolderPath = "../../docs/csv/samples/"
resultPath = '../Regression/input.csv'

# Function used to extract aggregated metrics from 1 repository
def processCSV(dir, df):
    actorPath = csvFolderPath + dir + '/processed/nodes.csv'
    ticketsPath = csvFolderPath + dir + '/processed/edges.csv'

    repo = dir
    # Extract metrics from actor file
    actors = pd.read_csv(actorPath, sep=',', encoding='utf-8', error_bad_lines=False)
    actors_rows = actors.shape[0]
    ntop = int(round((actors_rows * 0.05), 0))

    actorsTop = actors.sort_values(by='total influence', ascending=False).head(ntop)
    top_avg_deg_cent = round(actorsTop['Degree centrality'].mean(), 4)
    top_avg_betw_osc = round(actorsTop['Betweenness centrality oscillation'].mean(), 4)
    top_avg_sentiment = round(actorsTop['avg sentiment'].mean(), 4)
    top_avg_complexity = round(actorsTop['avg complexity'].mean(), 4)
    top_avg_influence = round(actorsTop['total influence'].mean(), 4)
    top_avg_influence_pm = round(actorsTop['average influence per message'].mean(), 4)
    top_avg_contrib = round(actorsTop['Contribution index'].mean(), 4)
    top_avg_contrib_oscil = round(actorsTop['Contribution index oscillation'].mean(), 4)

    actorsConnected = actors.loc[actors['Degree centrality'] > 1]
    avg_deg_cent = round(actorsConnected['Degree centrality'].mean(), 4)
    avg_betw_osc = round(actorsConnected['Betweenness centrality oscillation'].mean(), 4)
    avg_influence = round(actorsConnected['total influence'].mean(), 4)
    avg_influence_pm = round(actorsConnected['average influence per message'].mean(), 4)
    avg_contrib = round(actorsConnected['Contribution index'].mean(), 4)
    avg_contrib_oscil = round(actorsConnected['Contribution index oscillation'].mean(), 4)
    avg_sentiment = round(actorsConnected['avg sentiment'].mean(), 4)
    avg_complexity = round(actorsConnected['avg complexity'].mean(), 4)

    perc_connected = round((actors.loc[actors['Degree centrality'] >= ntop].shape[0]) / actors_rows, 4)
    perc_isolated = round((actors.loc[actors['Degree centrality'] == 1].shape[0]) / actors_rows, 4)

    # Extract metrics from ticket file

    edges = pd.read_csv(ticketsPath, sep=',', encoding='utf-8', error_bad_lines=False)

    edges_rows = edges.shape[0]
    perc_closed_issues = round((edges.loc[edges['Status'] == 'closed'].shape[0]) / edges_rows, 4)
    perc_creation = round((edges.loc[edges['Edge_type'] == 'CREATION'].shape[0]) / edges_rows, 4)
    perc_solo = edges[['Name']].groupby('Name').size().reset_index(name='counts')
    perc_solo = round((perc_solo.loc[perc_solo['counts'] == 1].shape[0]) / perc_solo.shape[0], 4)

    result = df.append({'Avg_Degree_Centrality_Top': top_avg_deg_cent,
                        'Avg_Degree_Centrality': avg_deg_cent,
                        'Avg_Betweenness_Osc_Top': top_avg_betw_osc,
                        'Avg_Betweenness_Osc': avg_betw_osc,
                        'Percentage_Connected_Actors': perc_connected,
                        'Avg_Sentiment': avg_sentiment,
                        'Avg_Sentiment_Top': top_avg_sentiment,
                        'Avg_complextiy': avg_complexity,
                        'Avg_complexity_Top': top_avg_complexity,
                        'Avg_Influence_Top': top_avg_influence,
                        'Avg_Influence': avg_influence,
                        'Avg_Influence_Per_Message_Top': top_avg_influence_pm,
                        'Avg_Influence_Per_Message': avg_influence_pm,
                        'Avg_Contribution_Index_Top': top_avg_contrib,
                        'Avg_Contribution_Index': avg_contrib,
                        'Avg_Contribution_Index_Oscil_Top': top_avg_contrib_oscil,
                        'Avg_Contribution_Index_Oscil': avg_contrib_oscil,
                        'Percentage_Closed_Issues': perc_closed_issues,
                        'Percentage_Creations': perc_creation,
                        'Percentage_Isolated_Actors': perc_isolated,
                        'Percentage_Solo_Issues':perc_solo,
                        'Repository_Name': repo}, ignore_index=True)
    return result


# 1: Create DataFrame
df = pd.DataFrame(columns=['Avg_Degree_Centrality_Top',
                           'Avg_Degree_Centrality',
                           'Avg_Betweenness_Osc_Top',
                           'Avg_Betweenness_Osc',
                           'Percentage_Connected_Actors',
                           'Avg_Sentiment',
                           'Avg_Sentiment_Top',
                           'Avg_complextiy',
                           'Avg_complexity_Top',
                           'Avg_Influence_Top',
                           'Avg_Influence',
                           'Avg_Influence_Per_Message_Top',
                           'Avg_Influence_Per_Message',
                           'Avg_Contribution_Index_Top',
                           'Avg_Contribution_Index',
                           'Avg_Contribution_Index_Oscil_Top',
                           'Avg_Contribution_Index_Oscil',
                           'Percentage_Isolated_Actors',
                           'Percentage_Closed_Issues',
                           'Percentage_Creations',
                           'Percentage_Solo_Issues',
                           'Repository_Name',
                           'Target'])

# 2: Process Input CSVs

for dirs in os.listdir(csvFolderPath):
    print(dirs)
    df = processCSV(dirs, df)
df.to_csv(resultPath, sep=',', encoding='utf-8', index=False)

print(1)
