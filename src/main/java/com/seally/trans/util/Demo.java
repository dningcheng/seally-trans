package com.seally.trans.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;

public class Demo {

	public static void main(String[] args) {
		//lambdaSortTest();//排序测试
		
		//lambdaInterFuncTest();//lambda定义接口函数实现测试
		
		lambdaStreamTest();
	}
	
	/**
	 * 使用lambda表达式排序
	 */
	public static void lambdaSortTest(){
		
		//数组排序
		String[] names = new String[]{"aaa","abc","bbb","aab","aa","dd"};
		System.out.println("排序前："+JSON.toJSONString(names));
		Arrays.sort(names, (String name1, String name2) -> (name1.compareTo(name2)));
		System.out.println("排序后："+JSON.toJSONString(names));
		
		//集合排序
		List<String> names2 = Arrays.asList(names);
		System.out.println("排序前："+JSON.toJSONString(names2));
		Collections.sort(names2, (name1,name2) -> (name1.compareTo(name2)));
		System.out.println("排序后："+JSON.toJSONString(names2));
		
		//结合自定义对象
		List<UserInfo> users = new ArrayList<>();users.add(new UserInfo(12, "张三"));users.add(new UserInfo(18, "王五"));users.add(new UserInfo(9, "李四"));
		System.out.println("排序前："+JSON.toJSONString(users));
		Collections.sort(users, (u1,u2) -> (u1.compareTo(u2)));
		System.out.println("排序后："+JSON.toJSONString(users));
		
	}
	
	/**
	 * 使用lambda表达式实现函数式接口
	 */
	public static void lambdaInterFuncTest(){
		
		CalcSum sumInstance1 = (a,b) -> {a*=2;b*=2; return (a+b);};//参数需要多步处理
		
		CalcSum sumInstance2 = (a,b) -> {return (a+b);};//只有返回值时去掉{}同时return可省略
		
		CalcSum sumInstance3 = (a,b) -> (a+b);//如果无需对参数进行额外处理，可直接返回
		
		CalcSum sumInstance4 = (a,b) -> a+b;//如果无需对参数进行额外处理，可直接返回
		
		System.out.println(sumInstance1.sum(12, 13));
		
		System.out.println(sumInstance2.sum(12, 13));
		
		System.out.println(sumInstance3.sum(12, 13));
		
		System.out.println(sumInstance4.sum(12, 13));
		
	}
	
	/**
	 * jdk8 stream 测试
	 */
	public static void lambdaStreamTest(){
		
		/**==================== 一、获得stream的方法 ============================ **/
		//方式1、使用集合接口Collection的默认方法stream
		List<Integer> ints = Arrays.asList(1,2,2,null,5,6,6,8,9,10);
		Stream<Integer> stream1 = ints.stream();
		
		//方式2、使用类Stream的静态方法of
		Stream<Integer> stream2 = Stream.of(1,2,3,3,5,6,7,8,9,10);//可变参数
		
		/**==================== 二、转换stream的方法，打印结果用到了汇聚方法collect ============================ **/
		//方法1、filter 入参接受一个过滤函数，该函数返回布尔值,根据布尔接口保留或是丢弃该元素
		Stream<Integer> filterResultStream = Stream.of(1,2,3,null,5,6,null,8,9,10).filter(num -> num != null);
		System.out.println(JSON.toJSONString(filterResultStream.collect(Collectors.toList())));//[1,2,3,5,6,8,9,10]
		
		//方法2、distinct 依赖元素的equels方法去除重复的元素
		Stream<Integer> distinctResultStream = Stream.of(1,2,3,3,3,6).distinct();
		System.out.println(JSON.toJSONString(distinctResultStream.collect(Collectors.toList())));//[1,2,3,6]
		
		//方法3、map 对于Stream中包含的元素使用给定的转换函数进行转换操作，新生成的Stream只包含转换生成的元素。这个方法有三个对于原始类型的变种方法，分别是：mapToInt、mapToLong和mapToDouble
		Stream<Integer> mapResultStream = Stream.of(1,2,3,3,5,6,7,8,9,10).map(num -> num * 2);
		System.out.println(JSON.toJSONString(mapResultStream.collect(Collectors.toList())));// [2,4,6,6,10,12,14,16,18,20]
		
		//比如mapToInt就是把原始Stream转换成一个新的Stream，这个新生成的Stream中的元素都是int类型。之所以会有这样三个变种方法，可以免除自动装箱/拆箱的额外消耗
		IntStream mapToIntResultStream = Stream.of(1,2,3,3,5,6,7,8,9,10).mapToInt(num -> num * 2);
		System.out.println(JSON.toJSONString(mapToIntResultStream.boxed().collect(Collectors.toList())));//注意mapToIntResultStream调用boxed会装换成为Stream<Integer>,然后就方便收集为List
		
		//方法4、flatMap flatMap与map的区别在于 flatMap是将一个流中的每个值都转成一个个流，然后再将这些流扁平化成为一个流
		Stream<String> flatMapResultStream = Stream.of(1,2,3).flatMap(num -> Arrays.asList(num+"a",num+"b",num+"c").stream());
		System.out.println(JSON.toJSONString(flatMapResultStream.collect(Collectors.toList())));// ["1a","1b","1c","2a","2b","2c","3a","3b","3c"]
		
		//方法5、peek 生成一个包含原Stream的所有元素的新Stream，接受一个消费函数（Consumer实例），新Stream每个元素都会执行给定的消费函数，peek接收一个没有返回值的λ表达式，可以做一些输出，外部处理等。map接收一个有返回值的λ表达式，之后Stream的泛型类型将转换为map参数λ表达式返回的类型
		Stream<Integer> peekResultStream = Stream.of(1,2,3).peek(num -> num+=2);
		System.out.println(JSON.toJSONString(peekResultStream.collect(Collectors.toList()))); // [1,2,3] 但是如果是map的话为 [3,4,5]
		
		//方法6、limit 对一个Stream进行截断操作，获取其前N个元素，如果原Stream中包含的元素个数小于N，那就获取其所有的元素
		Stream<Integer> limitResultStream = Stream.of(1,2,3,3,5,6,7,8,9,10).limit(3);
		System.out.println(JSON.toJSONString(limitResultStream.collect(Collectors.toList()))); //[1,2,3]
		
		//方法7、skip 返回一个丢弃原Stream的前N个元素后剩下元素组成的新Stream，如果原Stream中包含的元素个数小于N，那么返回空Stream
		Stream<Integer> skipResultStream = Stream.of(1,2,3,4,5,6,7,8,9,10).skip(3);
		System.out.println(JSON.toJSONString(skipResultStream.collect(Collectors.toList()))); //[4,5,6,7,8,9,10]
		
		
		/**==================== 三、汇聚stream的方法 ============================ **/
		//方法1、collect 该方法特别灵活，依赖Collectors类的系列静态方法可实现系列场景下的复杂操作，该方法有两个重载方法收集元素
		/**
		 * 场景A:直接装换为常用集合结果、使用Collectors的静态方法获取比如：Collectors.toList()、Collectors.toSet()等
		 */
		List<Integer> collectListResult = Stream.of(1,2,3,3,5,6,7,8,9,10).collect(Collectors.toList());
		ArrayList<Integer> collectArrayListResult = Stream.of(1,2,3,3,5,6,7,8,9,10).collect(Collectors.toCollection(ArrayList::new));//使用构造器引用方式指定具体类型
		Set<Integer> collectSetResult = Stream.of(1,2,3,3,5,6,7,8,9,10).collect(Collectors.toSet());
		
		/**
		 * 场景B:分组 对应数据结构为map及如何从stream使用collect方法生成map
		 * （1）使用Collectors.toMap()生成的收集器，用户需要指定如何生成Map的key和value。
		 * （2）使用Collectors.partitioningBy()生成的收集器，对元素进行二分区操作时用到。
		 * （3）使用Collectors.groupingBy()生成的收集器，对元素做group操作时用到。
		 */
		//（1）使用Collectors.toMap()，第一个函数决定如何生成key（注意，生成的key如果重复则会抛异常），第二个函数决定如何生成value
		Map<String, Integer> collectMapResult = Stream.of(1,2,3,4,5,6,7,8,9,10).collect(Collectors.toMap(num -> {return num > 5 ? num+"大": num+"小";}, num -> num+1));
		System.out.println(JSON.toJSONString(collectMapResult));
		
		List<UserInfo> users = new ArrayList<>();users.add(new UserInfo(12, "张三"));users.add(new UserInfo(18, "王五"));users.add(new UserInfo(9, "李四"));
		
		//（2）使用Collectors.partitioningBy()，将元素按照某种条件(比如年龄>=12)分为互补的两部分(用下面的groupingBy方法也能够实现)
		Map<Boolean, List<UserInfo>> collect = users.stream().collect(Collectors.partitioningBy(s -> s.getAge() >= 12));//等价于 users.stream().collect(Collectors.partitioningBy(s -> {return s.getAge() >=  12 ? true : false;}));
		System.out.println(JSON.toJSONString(collect));
        
		//（3）使用Collectors.groupingBy()，将元素按照某个属性(比如姓名name)分到对应的组中
		Map<String, List<UserInfo>> collect2 = users.stream().collect(Collectors.groupingBy(UserInfo::getName));//等价于users.stream().collect(Collectors.groupingBy(s -> s.getName()))
		System.out.println(JSON.toJSONString(collect2));
		//将元素按照某个属性分组且自定义组别信息（用此也可以实现上面partitioningBy功能如collect参数为:Collectors.groupingBy(s -> {return s.getAge() >= 10 ? true : false;})）
		Map<String, List<UserInfo>> collect3 = users.stream().collect(Collectors.groupingBy(s -> {return s.getAge() < 10 ? "小孩" : "大人" ;}));
		System.out.println(JSON.toJSONString(collect3));
		
		//（4）使用Collectors.averagingInt()，求平均
		Double collectAvegeResult = Stream.of(1,2,3,3,5,6,7,8,9,10).collect(Collectors.averagingInt(num-> num));
		System.out.println(collectAvegeResult);
		
		//（5）使用Collectors.joining 进行拼接
		String joined1 = Stream.of("hello", "world", "!").collect(Collectors.joining()); //五参数五分隔
		System.out.println(joined1);// helloworld!
		String joined2 = Stream.of("hello", "world", "!").collect(Collectors.joining(",")); //参数：分隔符
		System.out.println(joined2);// hello,world,!
		String joined3 = Stream.of("hello", "world", "!").collect(Collectors.joining(",", "{", "}")); //参数1：分隔符  参数2：前缀符  参数3：后缀符
		System.out.println(joined3);// {hello,world,!}
		
		//方法2、sum 求和，注意：该方法只有IntStream、LongStream和DoubleStream才有
		int sumIntResult = Stream.of("s",2,3,4,5,6,7,8,9,10).mapToInt(num -> {if(num instanceof Integer){return (Integer)num;}else{return 0;}}).sum();
		System.out.println(sumIntResult);
		
		double sumDoubleResult = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).mapToDouble(num -> (Double)num).sum();
		System.out.println(sumDoubleResult);
		
		//方法3、max、min
		OptionalDouble max = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).mapToDouble(num -> (Double)num).max();
		System.out.println(max.isPresent() ? max.getAsDouble() : 0);
		
		Optional<Double> max2 = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).max((num1,num2) -> num1.compareTo(num2));
		System.out.println(max2.get());
		
		OptionalDouble min = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).mapToDouble(num -> (Double)num).min();
		System.out.println(min.isPresent() ? min.getAsDouble() : 0);
		
		//方法4、计数
		long countResult = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).count();
		System.out.println(countResult);
		
		//方法5、reduce reduce方法非常的通用，前面介绍的count，sum等都可以使用其实现，reduce擅长的是生成一个值，如果想要从Stream生成一个集合或者Map等复杂的对象使用collect，reduce方法有三个override的方法
		//（1）形式A：Optional<T> reduce(BinaryOperator<T> accumulator); 接受一个BinaryOperator类型的参数，我们可以用lambda表达式来
		//reduce方法接受一个函数，这个函数有两个参数，第一个参数是上次函数执行的返回值（也称为中间结果），第二个参数是stream中的元素，这个函数把这两个值相加，得到的和会被赋值给下次执行这个函数的第一个参数
		Optional<Double> reduceSumResult = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).reduce((result,ele) -> result+ele);//唯一参数：接受两个参数的函数
		System.out.println(reduceSumResult.isPresent() ? reduceSumResult.get() : 0);
		
		//获取最长的单词
		Optional<String> longest = Stream.of("I", "love", "you", "too").reduce((s1, s2) -> s1.length()>=s2.length() ? s1 : s2);
		System.out.println(longest.get());
		
		//（2）形式B：T reduce(T identity, BinaryOperator<T> accumulator); 允许用户提供一个循环计算的初始值，如果Stream为空，就直接返回该值。而且这个方法不会返回Optional，因为其不会出现null值
		Double reduceSumResult2 = Stream.of(1D,20.5D,3D,4.5D,5D,6D,7D,8D,9D,10D).reduce(0D,(result,ele) -> result+ele);//参数1：循环计算的初始值  参数2：接受两个参数的函数
		System.out.println(reduceSumResult2);
		
		//其它方法
		/**
		 * – allMatch：是不是Stream中的所有元素都满足给定的匹配条件
		 * – anyMatch：Stream中是否存在任何一个元素满足匹配条件
		 * – findFirst: 返回Stream中的第一个元素，如果Stream为空，返回空Optional
		 * – noneMatch：是不是Stream中的所有元素都不满足给定的匹配条件
		 * – max和min：使用给定的比较器（Operator），返回Stream中的最大|最小值
		 */
	}
	
}

interface CalcSum{
	int sum(int a,int b);//如果一个接口只有一个未实现的接口方法，则该接口可作为lambda表达式实例化的函数式接口
	
	static void hello(String name){
		System.out.println(name+ " 你好！");//static方法不影响函数式接口特性
	}
	
	default void say(String name){//default方法不影响函数式接口特性
		System.out.println(name+ " 你好！");
	}
}

class UserInfo implements Comparable<UserInfo>{
	private Integer age;
	private String name;
	
	public UserInfo(){}
	public UserInfo(Integer age,String name){this.age = age;this.name=name;}
	
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int compareTo(UserInfo o) {
		
		if(null == this.age){
			this.age = 0;
		}
		if(null == o.getAge()){
			o.setAge(0);
		}
		return this.getAge() - o.getAge();
	}
}