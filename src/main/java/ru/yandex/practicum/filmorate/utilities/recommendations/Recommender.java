package ru.yandex.practicum.filmorate.utilities.recommendations;

import java.util.*;

public class Recommender {
    private static final Boolean TREAT_EMPTY_AS_ZEROS = true; // when rates != 1, false needed
    private static final Integer HOW_MANY_SIMILAR_ITEMS_TO_ANALYZE = 2; //internal, subject for test

    private final Matrix matrix;
    private final Matrix preparedMatrix;
    private final Matrix similarities;

    public Recommender(Matrix matrix) {
        this.matrix = matrix;
        this.preparedMatrix = prepareMatrix();
        this.similarities = findSimilarities();
    }

    public List<Integer> getRecommendations(Integer userId, Optional<Integer> limit) {
        if (matrix == null ||
            userId == null ||
            matrix.isEmpty() ||
            Collections.max(matrix.getRowIndexes()) < 2 ||
            Collections.max(matrix.getColumnIndexes()) < 2 ||
            matrix.getColumn(userId) == null)
        {
            return List.of();
        }
        List<Integer> recommendations = getRecommendationsInternal(userId);
        if (limit.isEmpty()) {
            return recommendations;
        } else {
            return recommendations.subList(0, Integer.min(limit.get(), recommendations.size()));
        }
    }

    private List<Integer> getRecommendationsInternal(Integer userId) {
        if (!similarities.isEmpty()) {
            Map<Integer, Double> absentRatings = findAbsentRatings(userId);
            return absentRatings.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .collect(ArrayList::new,
                            (list, e) -> list.add(e.getKey()),
                            ArrayList::addAll);
        } else {
            return List.of();
        }
    }

    private Map<Integer, Double> findAbsentRatings(Integer userId) {
        Map<Integer, Double> absentRatings = new HashMap<>();
        for (Integer itemId : matrix.getRowIndexes()) {
            if (matrix.getValue(itemId, userId).isEmpty()) {
                Map<Integer, Double> mostSimilarPresentItems = getMostSimilarPresentItems(itemId, userId);
                Double absentRatingSum = mostSimilarPresentItems.keySet().stream()
                        .map(aDouble -> matrix.getValue(aDouble, userId).get() * mostSimilarPresentItems.get(aDouble))
                        .mapToDouble(e->e)
                        .sum();
                Double absentRatingDenom = mostSimilarPresentItems.values().stream().mapToDouble(e->e).sum();
                Double absentRating = absentRatingSum / absentRatingDenom;
                absentRatings.put(itemId, absentRating);
            }
        }
        return absentRatings;
    }

    private Map<Integer, Double> getMostSimilarPresentItems(Integer itemId, Integer userId) {
        Map<Integer, Double> presentItemsMap = similarities.getRow(itemId).entrySet().stream()
                .collect(
                        HashMap::new,
                        (map, e)-> {
                            if (matrix.getValue(e.getKey(), userId).isPresent()) { // item to compare is rated by user with userId
                                map.put(e.getKey(), e.getValue().get());
                            }
                        },
                        HashMap::putAll
                );
        List<Integer> mostSimilarItems = presentItemsMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(ArrayList::new,
                        (list, e) -> list.add(e.getKey()),
                        ArrayList::addAll
                );
        Map<Integer, Double> result = new HashMap<>();
        for (Integer key : mostSimilarItems.subList(0, Integer.min(mostSimilarItems.size(), HOW_MANY_SIMILAR_ITEMS_TO_ANALYZE))) {
            result.put(key, presentItemsMap.get(key));
        }
        return result;
    }

    private Matrix prepareMatrix() {
        Matrix preparedMatrix = new Matrix();

        for (Integer rowI : matrix.getRowIndexes()) {
            for (Integer columnI : matrix.getColumnIndexes()) {
                preparedMatrix.writeValue(rowI, columnI, matrix.getValue(rowI, columnI));
            }
        }

        for (Integer columnI : preparedMatrix.getColumnIndexes()) {
            Map<Integer, Optional<Double>> column = preparedMatrix.getColumn(columnI);
            Double avg = findAverageValue(column);
            for (Integer rowI : preparedMatrix.getRowIndexes()) {
                Optional<Double> valueToWrite = column.get(rowI);
                if (valueToWrite.isPresent()) {
                    valueToWrite = Optional.of(valueToWrite.get() - avg);
                }
                column.put(rowI, valueToWrite);
            }
        }

        return preparedMatrix;
    }


    private Matrix findSimilarities() {
        Matrix similarities = new Matrix();
        for (Integer rowI : preparedMatrix.getRowIndexes()) {
            for (Integer rowI2 : preparedMatrix.getRowIndexes()) {
                if (similarities.getValue(rowI2, rowI) != null && similarities.getValue(rowI2, rowI).isPresent()) {
                    similarities.writeValue(rowI, rowI2, similarities.getValue(rowI2, rowI));
                } else {
                    Map<Integer, Double> vector1 = simplifyVector(preparedMatrix.getRow(rowI));
                    Map<Integer, Double> vector2 = simplifyVector(preparedMatrix.getRow(rowI2));
                    Double similarity = findCosineSimilarity(vector1, vector2);
                    similarities.writeValue(rowI, rowI2, Optional.of(similarity));
                }
            }
        }
        return similarities;
    }

    private Map<Integer, Double> simplifyVector (Map<Integer, Optional<Double>> vector) {
        return vector.entrySet().stream()
                .collect(
                        HashMap::new,
                        (map,e) -> {
                            if (e.getValue().isPresent()) {
                                map.put(e.getKey(),e.getValue().get());
                            } else {
                                map.put(e.getKey(),(double)0);
                            }
                        },
                        HashMap::putAll
                );
    }


    private Double findAverageValue(Map<Integer, Optional<Double>> map) {
        return map.values().stream()
                .map(e -> {
                    if (e.isEmpty() && TREAT_EMPTY_AS_ZEROS) {
                        return Optional.of((double) 0);
                    }
                    return e;
                })
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .average()
                .getAsDouble();
    }

    //function accepts two vectors with non-null non-empty equal keys and double values
    private Double findCosineSimilarity(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        if (vector1 == null || vector2 == null) {
            return null;
        }
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return null;
        }
        if (vector1.containsKey(null) || vector2.containsKey(null)) {
            return null;
        }
        if (!vector1.keySet().equals(vector2.keySet())) {
            return null;
        }
        Double lenOfVector1 = findVectorLength(vector1);
        Double lenOfVector2 = findVectorLength(vector2);
        Double scalarProductOfVectors = findScalarProductOfVectors(vector1, vector2);
        return scalarProductOfVectors / (lenOfVector1 * lenOfVector2);
    }

    private Double findVectorLength(Map<Integer, Double> vector) {
        return Math.sqrt(vector.values().stream().map(e->Math.pow(e,2)).mapToDouble(e->e).sum());
    }

    private Double findScalarProductOfVectors(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        return vector1.entrySet().stream()
                .map(e->e.getValue()*vector2.get(e.getKey()))
                .mapToDouble(e->e)
                .sum();
    }


    public static Matrix generateTestMatrix() {
        Matrix testMatrix = new Matrix();
        testMatrix.writeValue(1, 1, Optional.of((double) 1));
        testMatrix.writeValue(1, 3, Optional.of((double) 3));
        testMatrix.writeValue(1, 6, Optional.of((double) 5));
        testMatrix.writeValue(1, 9, Optional.of((double) 5));
        testMatrix.writeValue(1, 11, Optional.of((double) 4));
        testMatrix.writeValue(2, 3, Optional.of((double) 5));
        testMatrix.writeValue(2, 4, Optional.of((double) 4));
        testMatrix.writeValue(2, 7, Optional.of((double) 4));
        testMatrix.writeValue(2, 10, Optional.of((double) 2));
        testMatrix.writeValue(2, 11, Optional.of((double) 1));
        testMatrix.writeValue(2, 12, Optional.of((double) 3));
        testMatrix.writeValue(3, 1, Optional.of((double) 2));
        testMatrix.writeValue(3, 2, Optional.of((double) 4));
        testMatrix.writeValue(3, 4, Optional.of((double) 1));
        testMatrix.writeValue(3, 5, Optional.of((double) 2));
        testMatrix.writeValue(3, 7, Optional.of((double) 3));
        testMatrix.writeValue(3, 9, Optional.of((double) 4));
        testMatrix.writeValue(3, 10, Optional.of((double) 3));
        testMatrix.writeValue(3, 11, Optional.of((double) 5));
        testMatrix.writeValue(4, 2, Optional.of((double) 2));
        testMatrix.writeValue(4, 3, Optional.of((double) 4));
        testMatrix.writeValue(4, 5, Optional.of((double) 5));
        testMatrix.writeValue(4, 8, Optional.of((double) 4));
        testMatrix.writeValue(4, 11, Optional.of((double) 2));
        testMatrix.writeValue(5, 3, Optional.of((double) 4));
        testMatrix.writeValue(5, 4, Optional.of((double) 3));
        testMatrix.writeValue(5, 5, Optional.of((double) 4));
        testMatrix.writeValue(5, 6, Optional.of((double) 2));
        testMatrix.writeValue(5, 11, Optional.of((double) 2));
        testMatrix.writeValue(5, 12, Optional.of((double) 5));
        testMatrix.writeValue(6, 1, Optional.of((double) 1));
        testMatrix.writeValue(6, 3, Optional.of((double) 3));
        testMatrix.writeValue(6, 5, Optional.of((double) 3));
        testMatrix.writeValue(6, 8, Optional.of((double) 2));
        testMatrix.writeValue(6, 11, Optional.of((double) 4));
        return testMatrix;
    }
}
