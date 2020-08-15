package testing

import(

	"fmt"
//"airavata_api"

//	"net/http"
)


func main(){
	a:=10
	p:=&a
	
	customlist:=[]int{1,3,4,56,6,7,}
	println("vale of a is ",a)
	println("address of a is ",p)
	println("value of a is ",*p)
	
	changeVal(p)
	println("changed val",a)
	printList(customlist)

	type abc struct {
		name string
		postalcode int

	}

	val:= abc{name:"Satyam",postalcode:94040}

	fmt.Println(val.name)
	fmt.Println(val.postalcode)
	fmt.Printf("%v",val.postalcode)
}
func printList(customlist []int)  {


	for _,i:= range customlist{

			print(i," ")
	}
	println()

	print(customlist[0])
	for j:=1; j<len(customlist);j++{

		print(" ",customlist[j])
	}

}

func  changeVal(a*  int)  int {
	*a=12
	println("new value",*a)
	return *a;
}
