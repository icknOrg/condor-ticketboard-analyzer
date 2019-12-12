import pandas as pd
import os

csvFolderPath = "../../docs/csv/samples/"
resultPath = '../Regression/input.csv'


# csv structure:
# Target: Stars/Pullreq
# Attributes:
# - Avg. degree centrality of Top 5% actors by influence
# - Avg. Betweenness Oscillation of Top 5% actors by influence
# - % of actors with direct connections to at least 5% of the non-isolated actors
# - Avg. sentiment
# - Avg. sentiment of Top 5% actors by influence
# - Avg. comlexity
# - Avg. complexity of Top 5% actors by influence
# - Avg. influence of Top 5% actors by influence
# - Avg. contribution index of Top 5% actors by influence
# - % of isolated people

# Function used to extract aggregated metrics from 1 repository
def processCSV(dir, df):
    actorPath = csvFolderPath+dir+'/processed/nodes.csv'
    ticketsPath = csvFolderPath + dir + '/processed/edges.csv'

    repo = dir
    # Extract metrics from actor file
    actors = pd.read_csv(actorPath, sep=',', encoding='utf-8', error_bad_lines=False)
    nrows = actors.shape[0]
    ntop = int(round((nrows * 0.05), 0))
    actorsTop = actors.sort_values(by='total influence', ascending=False).head(ntop)

    top_avg_deg_cent = round(actorsTop['Degree centrality'].mean(), 4)
    top_avg_betw_osc = round(actorsTop['Betweenness centrality oscillation'].mean(), 4)
    top_avg_sentiment = round(actorsTop['avg sentiment'].mean(), 4)
    top_avg_complexity = round(actorsTop['avg complexity'].mean(), 4)
    top_avg_influence = round(actorsTop['total influence'].mean(), 4)
    top_avg_contrib = round(actorsTop['Contribution index'].mean(), 4)

    avg_sentiment = round(actors['avg sentiment'].mean(), 4)
    avg_complexity = round(actors['avg complexity'].mean(), 4)

    perc_connected = round((actors.loc[actors['Degree centrality'] >= ntop].shape[0]) / nrows, 4)
    perc_isolated = round((actors.loc[actors['Degree centrality'] == 1].shape[0]) / nrows, 4)

    # Extract metrics from ticket file

    edges = pd.read_csv(ticketsPath, sep=',', encoding='utf-8', error_bad_lines=False)

    result = df.append({'Avg_Degree_Centrality_Top': top_avg_deg_cent,
                        'Avg_Betweenness_Osc_Top': top_avg_betw_osc,
                        'Percentage_Connected_Actors': perc_connected,
                        'Avg_Sentiment': avg_sentiment,
                        'Avg_Sentiment_Top': top_avg_sentiment,
                        'Avg_complextiy': avg_complexity,
                        'Avg_complexity_Top': top_avg_complexity,
                        'Avg_Influence_Top': top_avg_influence,
                        'Avg_Contribution_Index_Top': top_avg_contrib,
                        'Percentage_Isolated_Actors': perc_isolated,
                        'Repository_Name': repo}, ignore_index=True)
    return result


# 1: Create DataFrame
df = pd.DataFrame(columns=['Avg_Degree_Centrality_Top', 'Avg_Betweenness_Osc_Top',
                           'Percentage_Connected_Actors', 'Avg_Sentiment', 'Avg_Sentiment_Top', 'Avg_complextiy',
                           'Avg_complexity_Top', 'Avg_Influence_Top', 'Avg_Contribution_Index_Top',
                           'Percentage_Isolated_Actors', 'Repository_Name', 'Target'])

# 2: Process Input CSVs

for dirs in os.listdir(csvFolderPath):
    print(dirs)
    df = processCSV(dirs, df)
df.to_csv(resultPath, sep=',', encoding='utf-8', index=False)

print(1)
