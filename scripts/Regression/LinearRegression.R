library(caTools)
# library(regclass)

path <- "scripts/Regression/input.csv"
data <- read.csv(file=path, header=TRUE)
str(data)

# data_split = sample.split(data, SplitRatio = 0.75)
# train <- subset(data, data_split == TRUE)
# test <-subset(data, data_split == FALSE)

model <- lm(Target ~ Group_Influence +
              Group_Percentage_AWVCI_Increase_Monthly +
              Group_AWVCI +
              Group_Percentage_Density_Increase_Monthly +
              Group_Density +
              Percentage_Connected_Actors +
              Gini_Sentiment +
              Gini_Sentiment_Top +
              Gini_complexity +
              Percentage_Isolated_Actors +
              Percentage_Closed_Issues +
              Percentage_Solo_Issues, data = data)

summary(model)

# visualize_model(M)
# plot(model)

model <- lm(Target ~ Group_Percentage_AWVCI_Increase_Monthly +
              Gini_complexity +
              Percentage_Closed_Issues, data = data)

summary(model)