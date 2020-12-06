
const data = parse.(Int, readlines(open("../input/01.txt")))

function ans1()
  nums = Set(data)
  for number in data:
    other = 2020 - number
    if other ∈ nums 
      return other
    end
  end
end

println("Answer1: ", ans1())
