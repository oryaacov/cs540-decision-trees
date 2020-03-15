import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
  private DecTreeNode root;
  //ordered list of class labels
  private List<String> labels; 
  //ordered list of attributes
  private List<String> attributes; 
  //map to ordered discrete values taken by attributes
  private Map<String, List<String>> attributeValues; 
  
  /**
   * Answers static questions about decision trees.
   */
  DecisionTreeImpl() {
    // no code necessary this is void purposefully
  }

  /**
   * Build a decision tree given only a training set.
   * 
   * @param train: the training set
   */
  DecisionTreeImpl(DataSet train) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;

  }

  @Override
  public String classify(Instance instance) {

    // TODO: add code here
    return "";
  }

  @Override
  public void rootInfoGain(DataSet train) {
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    for (String attribute : attributes) {
      System.out.format(
              "%s %.5f\n",
              attribute,
              info_gain(attribute, train.instances)
      );
    }
  }
  
  @Override
  public void printAccuracy(DataSet test) {
    // TODO: add code here
  }
    /**
   * Build a decision tree given a training set then prune it using a tuning set.
   * ONLY for extra credits
   * @param train: the training set
   * @param tune: the tuning set
   */
  DecisionTreeImpl(DataSet train, DataSet tune) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
    // only for extra credits
  }
  
  @Override
  /**
   * Print the decision tree in the specified format
   */
  public void print() {

    printTreeNode(root, null, 0);
  }

  /**
   * Prints the subtree of the node with each line prefixed by 4 * k spaces.
   */
  public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < k; i++) {
      sb.append("    ");
    }
    String value;
    if (parent == null) {
      value = "ROOT";
    } else {
      int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
      value = attributeValues.get(parent.attribute).get(attributeValueIndex);
    }
    sb.append(value);
    if (p.terminal) {
      sb.append(" (" + p.label + ")");
      System.out.println(sb.toString());
    } else {
      sb.append(" {" + p.attribute + "?}");
      System.out.println(sb.toString());
      for (DecTreeNode child : p.children) {
        printTreeNode(child, p, k + 1);
      }
    }
  }


  private float info_gain(String attr, List<Instance> instances) {
    return entropy(instances) - entropy(instances, attr);
  }
 /* private float entropy(List<Instance> instances) {
    float sum = 0.0f;
    for (String label : labels) {
      int matches = 0;
      int label_index = labels.indexOf(label);
      for (Instance instance : instances) {
        if (instance.label.equals(labels.get(label_index))) {
          matches++;
        }
      }
      float prob = ((float)matches)/((float)instances.size());
      if (instances.size() != 0 && prob > 0.0f) {
        sum -= prob * Math.log(prob)/Math.log(2.0);
      }

    }
    return sum;
  }*/
 private static double Log2(double n) {
   return Math.log(n) / Math.log(2);
 }



  private float entropy(List<Instance> instances) {
    float sum = 0.0f;
    int matches = 0;
    for (Instance instance : instances) {
        if (instance.label.equals(labels.get(0))) {
          matches++;
        }
    }
    float prob = ((float)matches)/((float)instances.size());
    sum -= prob * Log2(prob);
    prob =((float)(instances.size()-matches))/((float)instances.size());
    sum -= prob * Log2(prob);
    return sum;
  }

  private float entropy(List<Instance> instances, String attr) {
    //entropy(attr) = sum_over_values(prob(attr = value)*entropy(attr, value))
    float sum = 0.0f;
    for (String value : attributeValues.get(attr)) {
      int matches = 0;
      for (Instance instance : instances) {
        if (Integer.parseInt(instance.attributes.get(attributes.indexOf(attr))) == attributeValues.get(attr).indexOf(value)) {
          ++matches;
        }
      }
      if (instances.size() != 0) {
        sum += ((float)matches)/((float)instances.size())*entropy(instances, attr, value);
      }
    }

    return sum;
  }


  private float entropy(List<Instance> instances, String attr, String value) {
    float sum = 0.0f;
    List<Instance> matching_instances = new ArrayList<Instance>();
    for (Instance instance : instances) {
      if (Integer.parseInt(instance.attributes.get(attributes.indexOf(attr))) == attributeValues.get(attr).indexOf(value)) {
        matching_instances.add(instance);
      }
    }

    for (String label : labels) {
      int matches = 0;
      int label_index = labels.indexOf(label);
      for (Instance instance : matching_instances) {
        if (Integer.parseInt(instance.label) == label_index) {
          ++matches;
        }
      }
      float prob = ((float)matches)/((float)matching_instances.size());
      if (matching_instances.size() != 0 && prob > 0.0f) {
        sum -= prob * Math.log(prob)/Math.log(2.0);
      }
    }
    return sum;
  }
  /**
   * Helper function to get the index of the label in labels list
   */
  private int getLabelIndex(String label) {
    for (int i = 0; i < this.labels.size(); i++) {
      if (label.equals(this.labels.get(i))) {
        return i;
      }
    }
    return -1;
  }
 
  /**
   * Helper function to get the index of the attribute in attributes list
   */
  private int getAttributeIndex(String attr) {
    for (int i = 0; i < this.attributes.size(); i++) {
      if (attr.equals(this.attributes.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
   */
  private int getAttributeValueIndex(String attr, String value) {
    for (int i = 0; i < attributeValues.get(attr).size(); i++) {
      if (value.equals(attributeValues.get(attr).get(i))) {
        return i;
      }
    }
    return -1;
  }
}
