import javax.swing.tree.TreeNode;
import java.util.*;

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
    List<Instance> instances = train.instances;
    double[] attrGains=new double[attributes.size()];
    String currentPluralityValue = pluralityValue(instances);

    if (instances==null || instances.isEmpty()){
      this.root=null;//if there no instances don't generate any tree
    }
    if(sameLabelForAll(instances))
    {
      String label = instances.get(0).label;
      this.root = new DecTreeNode(label, null, null, true); // attribute = null, parentAttribute = null, terminal = true
      return;
    }

    int mostImportantAttrIndex = ImportanceAttrIndex(attributes,instances,attrGains);
    DecTreeNode node = new DecTreeNode(null, attributes.get(mostImportantAttrIndex), null, false);
    this.root = node;
    attrGains[mostImportantAttrIndex]=-1.0f;

    for(String value : attributeValues.get(attributes.get(mostImportantAttrIndex))) {
      buildTree(filterInstancesByAttr(instances, attributes.get(mostImportantAttrIndex), value), node, value,currentPluralityValue, Arrays.copyOf(attrGains,attrGains.length));
    }
  }



  // recursive method for building the tree one layer at a time, passes to each son the entire information of the dataset
  private void buildTree(List<Instance> instances, DecTreeNode parent, String parentValue,String parentPluralityValue,double[] attrGains)
  {
    String currentPluralityValue = pluralityValue(instances);
    if (instances.isEmpty()){
      parent.addChild(new DecTreeNode(parentPluralityValue,null,parentValue,true));
      return;
    }
    if(isAllNegative(attrGains))
    {
      DecTreeNode node = new DecTreeNode(currentPluralityValue, null, parentValue, true);
      parent.addChild(node);
      return;
    }

    if(sameLabelForAll(instances))
    {
      parent.addChild(new DecTreeNode(instances.get(0).label, null, parentValue, true));
      return;
    }

    int mostImportantAttrIndex = ImportanceAttrIndex(attributes,instances,attrGains);
    DecTreeNode node =new DecTreeNode(null, attributes.get(mostImportantAttrIndex), parentValue, false);
    parent.addChild(node);
    //mark as removed
    attrGains[mostImportantAttrIndex]=-1.0f;
    for(String value : attributeValues.get(attributes.get(mostImportantAttrIndex))){
      buildTree(filterInstancesByAttr(instances, attributes.get(mostImportantAttrIndex), value), node, value,currentPluralityValue,Arrays.copyOf(attrGains,attrGains.length));
    }
  }
  @Override
  public String classify(Instance instance) {
    DecTreeNode node = this.root;
    while(!node.terminal)
    {
      boolean found = false;
      for (int i = 0; i < node.children.size(); i++)
      {
        DecTreeNode child = node.children.get(i);
        if (child.parentAttributeValue.equals(instance.attributes.get(getAttributeIndex(node.attribute))))
        {
          node = child;
          found = true;
          break;
        }
      }
      if(!found)
        return null;
    }
    return node.label;
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
              infoGain(attribute, train.instances)
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

  }


  private boolean sameLabelForAll(List<Instance> instances)
  {
    for(int i = 1; i < instances.size(); i++)
    {
      if(!instances.get(0).label.equals(instances.get(i).label))
        return false;
    }
    return true;
  }


  private void calculateAttributesGain(List<Instance> instances,List<String> attributes,double[] attrGains){
    for (int i=0;i< attributes.size();i++) {
      if (attrGains[i] >= 0) {
        double temp = infoGain(attributes.get(i), instances);
        attrGains[i] = temp;
      }
    }
  }

  private int ImportanceAttrIndex(List<String> attributes,  List<Instance> instances,double[] attrGains) {
    double max =-1;
    int index=0;
    calculateAttributesGain(instances,attributes,attrGains);
    for (int i = 0; i < attrGains.length; i++)
    {
      if (attrGains[i]>max){
        max=attrGains[i];
        index=i;
      }
    }
    return index;
  }

  private boolean isAllNegative(double[] arr){
      for(double num :arr){
        if (num>=0){
          return false;
        }
    }
      return true;
  }




  private List<Instance> filterInstancesByAttr(List<Instance> instances, String attribute, String attributeValue)
  {
    int attributeIndex = getAttributeIndex(attribute);
    List<Instance> newInstances = new ArrayList();
    for(Instance instance : instances) {
      if(instance.attributes.get(attributeIndex).equals(attributeValue))
        newInstances.add(instance);
    }
    return newInstances;
  }

  private List<Integer>  ToList(int[] a)
  {
    List<Integer> list = new ArrayList<>(a.length);
    for (int i=0;i<a.length;i++){
      list.add(a[i]);
    }
    return list;
  }

  // boundary checking avoided for readability
  private String pluralityValue (List<Instance> instances) {
    int[] labelsCounts = new int[labels.size()];
    for(Instance instance : instances)
    {
      labelsCounts[labels.indexOf(instance.label)]++;
    }
    List<Integer> countList=ToList(labelsCounts);
    return labels.get(countList.indexOf(Collections.max(countList)));
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


  private float infoGain(String attr, List<Instance> instances) {
    return entropy(instances) - entropy(instances, attr);
  }

 private static double log2(double n) {
   return Math.log(n) / Math.log(2);
 }


  private int CountPositiveLabels(List<Instance> instances){
   int res =0 ;
   for (Instance instance : instances) {
      if (instance.label.equals(labels.get(0))) {
        res++;
      }
    }
   return res;
  }


  private float entropy(List<Instance> instances) {
    float sum = 0.0f;
    int positiveInstances=CountPositiveLabels(instances);
    float prob = ((float)positiveInstances)/((float)instances.size());
    if (prob>0) {
      sum -= prob * log2(prob);
    }
    prob =((float)(instances.size()-positiveInstances))/((float)instances.size());
    if (prob>0) {
      sum -= prob * log2(prob);
    }
    return sum;
  }

  private float entropy(List<Instance> instances, String attr) {
    //entropy(attr) = sum_over_values(prob(attr = value)*entropy(attr, value))
    float sum = 0.0f;
    for (String value : attributeValues.get(attr)) {
      List<Instance> tmp = new ArrayList<>();
      for (Instance instance : instances) {
        if (instance.attributes.get(attributes.indexOf(attr)).equals(value)) {
          tmp.add(instance);
        }
      }
      if (tmp.size() != 0) {
        sum += ((float)tmp.size())/((float)instances.size())*entropy(tmp);
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
