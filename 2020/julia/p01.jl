const raw = readlines(open(joinpath(@__DIR__, "../input/01.txt")))
const data = parse.(Int, raw)

function ans1(data)
  nums = Set(data)
  for number in data
    other = 2020 - number
    if other ∈ nums 
      return other * number
    end
  end
end

println("Answer1: ", ans1(data))

function ans2(data)
  nums = Set(data)
  for (i, number) in enumerate(data)
    for number2 in data[i:end]
      remaining = 2020 - number - number2
      if remaining ∈ nums 
        return remaining * number * number2
      end
    end
  end
end

println("Answer2: ", ans2(data))


