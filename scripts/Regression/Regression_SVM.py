import pandas as pd

import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split

from sklearn.model_selection import GridSearchCV

from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.svm import SVR

createPlots = True
plotFolderPath = './Plots_SVM/'

# read csv
df = pd.read_csv('input.csv', sep=',', encoding='utf-8').drop('Repository_Name', axis=1)

# columns with correlation over 0.1 or under -0.1 with target
usefulColumns = ['Group_Percentage_AWVCI_Increase_Monthly',
                 'Gini_complexity',
                 'Percentage_Closed_Issues',
                 'Target']
df = df[usefulColumns]

# scale
scaler = MinMaxScaler()
df[['Group_Percentage_AWVCI_Increase_Monthly']] = scaler.fit_transform(
df[['Group_Percentage_AWVCI_Increase_Monthly']]
)

if not createPlots:
    n_runs = 100
    performance = pd.DataFrame(columns=['Run', 'MSE', 'R2','epsilon','C'])

    for i in range(n_runs):
        # train test split
        train, test = train_test_split(df, test_size=0.1)
        X_train = train.drop('Target', axis=1)
        y_train = train.loc[:, ['Target']]
        X_test = test.drop('Target', axis=1)
        y_test = test.loc[:, ['Target']]

        # create and train model
        parameters = {
            'epsilon': [0.1, 0.2, 0.3, 0.4, 0.5],
            'C': [0.1, 0.5, 1,5,10,25,50]
        }
        regr = GridSearchCV(estimator=SVR(), param_grid=parameters, n_jobs=-1, cv=5, scoring='r2')
        regr.fit(X_train, y_train)

        # predict and evaluate
        y_pred = regr.predict(X_test)
        mse = mean_squared_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        performance = performance.append({'Run': i, 'MSE': mse, 'R2': r2, 'epsilon': regr.best_estimator_.epsilon,
                                          'C': regr.best_estimator_.C}, ignore_index=True)
        performance.to_csv('regression_performance_SVM.csv', sep=',', encoding='utf-8', index=False)
else:
    # train test split
    train, test = train_test_split(df, test_size=0.1)
    X_train = train.drop('Target', axis=1)
    y_train = train.loc[:, ['Target']]
    X_test = test.drop('Target', axis=1)
    y_test = test.loc[:, ['Target']]

    # create and train model
    parameters = {
        'epsilon': [0.1, 0.2, 0.3, 0.4, 0.5],
        'C': [0.1, 0.5, 1, 5, 10, 25, 50]
    }
    regr = GridSearchCV(estimator=SVR(), param_grid=parameters, n_jobs=-1, cv=5, scoring='r2')
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

