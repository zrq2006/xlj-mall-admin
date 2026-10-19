$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080'
Start-Sleep -Seconds 7

$login = Invoke-RestMethod -Uri "$base/admin/auth/login" -Method Post -ContentType 'application/json' -Body '{"username":"admin","password":"admin123"}'
$h = @{ Authorization = "Bearer $($login.data.token)" }

function Call($method, $path, $body) {
  $params = @{ Uri = "$base$path"; Method = $method; Headers = $h; UseBasicParsing = $true }
  if ($body) {
    $json = $body | ConvertTo-Json -Depth 5
    $params.ContentType = 'application/json; charset=utf-8'
    $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
  }
  try {
    $r = Invoke-WebRequest @params
    return [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
  } catch {
    $status = $_.Exception.Response.StatusCode.value__
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream(), [System.Text.Encoding]::UTF8)
    return "HTTP $status :: " + $reader.ReadToEnd()
  }
}

Write-Output '=== 品牌 CRUD ==='
$created = Call 'POST' '/admin/brands' @{ name = '接口测试品牌'; firstLetter = ''; logo = ''; description = '由自动化脚本创建'; sort = 900; status = 1 }
Write-Output $created
$brandId = ($created | ConvertFrom-Json).data.id
Write-Output (Call 'PUT' "/admin/brands/$brandId" @{ name = '接口测试品牌-改名'; logo = '🧪'; description = '已更新'; sort = 901; status = 0 })
Write-Output (Call 'DELETE' "/admin/brands/$brandId" $null)

Write-Output '=== 商品 CRUD ==='
$product = Call 'POST' '/admin/products' @{ name = '接口测试商品'; description = '测试描述'; categoryId = 16; brandId = 1; price = 12.5; marketPrice = 10; stock = 5; sales = 0; image = ''; status = 1 }
Write-Output $product
$productId = ($product | ConvertFrom-Json).data.id
Write-Output (Call 'PATCH' "/admin/products/$productId/status" @{ status = 0 })
Write-Output (Call 'DELETE' "/admin/products/$productId" $null)

Write-Output '=== 分类校验（删除有商品的分类应失败）==='
Write-Output (Call 'DELETE' '/admin/categories/16' $null)

Write-Output '=== 模板 CSV ==='
$csv = Invoke-WebRequest -Uri "$base/admin/products/template" -Headers $h -UseBasicParsing
$text = [System.Text.Encoding]::UTF8.GetString($csv.RawContentStream.ToArray())
Write-Output $text.Substring(0, [Math]::Min(160, $text.Length))

Write-Output '=== 批量导入（CSV）==='
$tmp = Join-Path $env:TEMP 'products-import-test.csv'
$content = "商品名称,分类,品牌,售价,同行价,库存,销量,商品图片,状态,描述`r`n导入测试商品A,日用百货,古驰,18.8,15,20,0,,上架,来自导入`r`n导入测试商品B,不存在的分类,,1,1,1,0,,上架,应失败`r`n"
[System.IO.File]::WriteAllText($tmp, $content, (New-Object System.Text.UTF8Encoding($false)))
$form = @{ file = Get-Item $tmp }
$r = Invoke-RestMethod -Uri "$base/admin/products/import" -Method Post -Headers $h -Form $form
$r | ConvertTo-Json -Depth 5

Write-Output '=== 清理导入的测试商品 ==='
$list = Invoke-RestMethod -Uri "$base/admin/products?page=1&size=5&name=%E5%AF%BC%E5%85%A5%E6%B5%8B%E8%AF%95" -Headers $h
foreach ($item in $list.data.list) { Call 'DELETE' "/admin/products/$($item.id)" $null | Out-Null }
Write-Output "cleaned $($list.data.list.Count)"
