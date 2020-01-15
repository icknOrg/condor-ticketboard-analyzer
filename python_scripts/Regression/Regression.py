import pandas as pd

import matplotlib.pyplot as plt
import seaborn as sn

from sklearn.model_selection import train_test_split

from sklearn.model_selection import GridSearchCV

from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.linear_model import ElasticNet

createPlots = False
plotFolderPath = './Plots/'

# read csv
df = pd.read_csv('input.csv', sep=',', encoding='utf-8').drop('Repository_Name', axis=1)

# Analyze correlation
sn.heatmap(df.corr(), annot=True)
# plt.show()
plt.close()

# columns that possibly decrease prediction quality
# badColumns = ['Group_Avg_Influence',
#               'Avg_Betweenness_Osc_Top',
#               'Avg_Influence_Top',
#               'Avg_Influence',
#               'Avg_Influence_Per_Message_Top',
#               'Avg_Influence_Per_Message',
#               'Avg_Contribution_Index_Top',
#               'Avg_Contribution_Index_Oscil_Top']
# df = df.drop(badColumns, axis=1)

# columns with correlation over 0.1 or under -0.1 with target
usefulColumns = ['Group_Influence',
                 'Group_Percentage_AWVCI_Increase_Monthly',
                 'Group_AWVCI',
                 'Group_Percentage_Density_Increase_Monthly',
                 'Group_Density',
                 'Percentage_Connected_Actors',
                 'Gini_Sentiment',
                 'Gini_Sentiment_Top',
                 'Gini_complextiy',
                 'Percentage_Isolated_Actors',
                 'Percentage_Closed_Issues',
                 'Percentage_Solo_Issues',
                 'Target']
df = df[usefulColumns]
sn.heatmap(df.corr(), annot=True)
plt.xticks(rotation=15)
# plt.show()
# plt.savefig('Correlation_Matrix.png',dpi=500)
plt.close()

# scale (ElasticNet supports normalization as a hyperparameter)
# scaler = MinMaxScaler()
# df[['Avg_Degree_Centrality_Top','Group_Betweenness_Centrality','Avg_Betweenness_Osc_Top','Percentage_Connected_Actors',
#     'Avg_Sentiment','Avg_Sentiment_Top','Avg_complextiy','Avg_complexity_Top','Avg_Influence_Top',
#     'Avg_Contribution_Index_Top','Percentage_Isolated_Actors','Percentage_Closed_Issues']] = scaler.fit_transform(
# df[['Avg_Degree_Centrality_Top','Group_Betweenness_Centrality','Avg_Betweenness_Osc_Top','Percentage_Connected_Actors',
#     'Avg_Sentiment','Avg_Sentiment_Top','Avg_complextiy','Avg_complexity_Top','Avg_Influence_Top',
#     'Avg_Contribution_Index_Top','Percentage_Isolated_Actors','Percentage_Closed_Issues']]
# )

if not createPlots:
    n_runs = 20
    performance = pd.DataFrame(columns=['Run', 'MSE', 'R2','alpha','l1_ratio'])

    for i in range(n_runs):
        # train test split
        train, test = train_test_split(df, test_size=0.1)
        X_train = train.drop('Target', axis=1)
        y_train = train.loc[:, ['Target']]
        X_test = test.drop('Target', axis=1)
        y_test = test.loc[:, ['Target']]

        # create and train model
        parameters = {
            'alpha': [0.1, 0.25, 0.5, 0.75, 1, 2, 5, 10, 25, 50, 100, 200],
            'l1_ratio': [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1],
            'normalize': [True]
        }
        regr = GridSearchCV(estimator=ElasticNet(), param_grid=parameters, n_jobs=-1, cv=5, scoring='r2')
        regr.fit(X_train, y_train)

        # predict and evaluate
        y_pred = regr.predict(X_test)
        mse = mean_squared_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        performance = performance.append({'Run': i, 'MSE': mse, 'R2': r2, 'alpha': regr.best_estimator_.alpha,
                                          'l1_ratio': regr.best_estimator_.l1_ratio}, ignore_index=True)

    performance.to_csv('regression_performance.csv', sep=',', encoding='utf-8', index=False)

if createPlots:
    # train test split
    train, test = train_test_split(df, test_size=0.1)
    X_train = train.drop('Target', axis=1)
    y_train = train.loc[:, ['Target']]
    X_test = test.drop('Target', axis=1)
    y_test = test.loc[:, ['Target']]

    # create and train model
    parameters = {
        'alpha': [0.1, 0.25, 0.5, 0.75, 1, 2, 5, 10, 25, 50, 100, 200],
        'l1_ratio': [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1],
        'normalize': [True],
        'selection': ['random','cyclic']

    }
    regr = GridSearchCV(estimator=ElasticNet(), param_grid=parameters, n_jobs=-1, cv=5, scoring='r2')
    regr.fit(X_train, y_train)

    # predict and evaluate
    y_pred = regr.predict(X_test)
    mse = mean_squared_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    # Plot outputs
    for column in X_train:
        plot_X = X_test.loc[:, column]
        fig = plt.figure()
        ax = fig.add_subplot(111)
        ax.scatter(X_train.loc[:, column], y_train, color='black', alpha=0.2)
        ax.scatter(plot_X, y_test, color='black')
        ax.scatter(plot_X, y_pred, color='red')
        ax.set_xlabel(column)
        ax.set_ylabel('Repository Stars divided by Pull Requests')
        ax.set_title('Predictions')
        plt.savefig(fname=plotFolderPath + column + '_scatter.png')
        plt.close()



# print('Best score:', regr.best_score_)
# print('Evaluation MSE: ' + str(mse))
# print('Evaluation R2: ' + str(r2))
# print('Coefficients: ')
# print(regr.best_estimator_.coef_)

