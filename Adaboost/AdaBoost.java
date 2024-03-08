import java.util.ArrayList;
import java.util.List;

public class Adaboost {
    private List<Double> weights;
    private List<Double> classifiers;

    public Adaboost() {
        weights = new ArrayList<>();
        classifiers = new ArrayList<>();
    }

    public void train(double[][] X, int[] y, int numIterations) {
        int numSamples = X.length;
        int numFeatures = X[0].length;

        // Initialize weights uniformly
        for (int i = 0; i < numSamples; i++) {
            weights.add(1.0 / numSamples);
        }

        // Adaboost iterations
        for (int t = 0; t < numIterations; t++) {
            // Train weak learner (here, we use a simple decision stump)
            double[] weakClassifier = trainWeakClassifier(X, y, weights);

            // Calculate weighted error
            double weightedError = 0.0;
            for (int i = 0; i < numSamples; i++) {
                if (predict(X[i], weakClassifier) != y[i]) {
                    weightedError += weights.get(i);
                }
            }

            // Calculate classifier weight
            double classifierWeight = 0.5 * Math.log((1 - weightedError) / weightedError);

            // Update sample weights
            for (int i = 0; i < numSamples; i++) {
                if (predict(X[i], weakClassifier) != y[i]) {
                    weights.set(i, weights.get(i) * Math.exp(classifierWeight));
                } else {
                    weights.set(i, weights.get(i) * Math.exp(-classifierWeight));
                }
            }

            // Normalize weights
            double sumWeights = 0.0;
            for (double weight : weights) {
                sumWeights += weight;
            }
            for (int i = 0; i < numSamples; i++) {
                weights.set(i, weights.get(i) / sumWeights);
            }

            // Add weak classifier and its weight to the ensemble
            classifiers.add(classifierWeight);
            classifiers.add(weakClassifier[0]);
            classifiers.add(weakClassifier[1]);
        }
    }

    private double[] trainWeakClassifier(double[][] X, int[] y, List<Double> weights) {
        int numSamples = X.length;
        int numFeatures = X[0].length;

        double[] bestWeakClassifier = new double[3]; // Format: [feature index, threshold, polarity]
        double minError = Double.POSITIVE_INFINITY;

        // Loop over features
        for (int featureIdx = 0; featureIdx < numFeatures; featureIdx++) {
            // Sort samples by feature value
            double[][] sortedSamples = sortSamples(X, y, weights, featureIdx);

            // Initialize cumulative sum of weights
            double posWeight = 0.0;
            double negWeight = 0.0;
            for (int i = 0; i < numSamples; i++) {
                if (y[i] == 1) {
                    posWeight += weights.get(i);
                } else {
                    negWeight += weights.get(i);
                }
            }

            // Loop over thresholds
            for (int i = 0; i < numSamples - 1; i++) {
                double threshold = (sortedSamples[featureIdx][i] + sortedSamples[featureIdx][i + 1]) / 2.0;
                double error = Math.min(posWeight + (negWeight - sortedSamples[featureIdx][i] * (i + 1)), // Positive polarity
                                         negWeight + (posWeight - sortedSamples[featureIdx][i] * (i + 1))); // Negative polarity
                if (error < minError) {
                    minError = error;
                    bestWeakClassifier[0] = featureIdx;
                    bestWeakClassifier[1] = threshold;
                    bestWeakClassifier[2] = (posWeight > negWeight) ? 1 : -1; // polarity
                }
            }
        }

        return bestWeakClassifier;
    }

    private double[][] sortSamples(double[][] X, int[] y, List<Double> weights, int featureIdx) {
        int numSamples = X.length;
        double[][] sortedSamples = new double[X.length][2];
        for (int i = 0; i < numSamples; i++) {
            sortedSamples[i][0] = X[i][featureIdx];
            sortedSamples[i][1] = y[i];
        }

        // Bubble sort based on feature value
        for (int i = 0; i < numSamples - 1; i++) {
            for (int j = 0; j < numSamples - i - 1; j++) {
                if (sortedSamples[j][0] > sortedSamples[j + 1][0]) {
                    // Swap feature values
                    double temp = sortedSamples[j][0];
                    sortedSamples[j][0] = sortedSamples[j + 1][0];
                    sortedSamples[j + 1][0] = temp;

                    // Swap labels
                    temp = sortedSamples[j][1];
                    sortedSamples[j][1] = sortedSamples[j + 1][1];
                    sortedSamples[j + 1][1] = temp;
                }
            }
        }

        return sortedSamples;
    }

    private int predict(double[] sample, double[] weakClassifier) {
        double featureValue = sample[(int) weakClassifier[0]];
        double threshold = weakClassifier[1];
        int polarity = (int) weakClassifier[2];
        return (featureValue <= threshold) ? polarity : -polarity;
    }

    public int predict(double[] sample) {
        double prediction = 0.0;
        for (int i = 0; i < classifiers.size(); i += 3) {
            prediction += classifiers.get(i) * predict(sample, new double[]{classifiers.get(i + 1), classifiers.get(i + 2)});
        }
        return (prediction >= 0) ? 1 : -1;
    }

    public static void main(String[] args) {
        // Sample data (replace this with your data)
        double[][] X = {{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}};
        int[] y = {1, 1, 1, -1, -1, -1, 1, 1};

        // Create and train Adaboost classifier
        Adaboost adaboost = new Adaboost();
        adaboost.train(X, y, 3); // Number of iterations

        // Predict a sample
        double[] sample = {3, 4};
        int prediction = adaboost.predict(sample);
        System.out.println("Prediction: " + prediction);
    }
}
